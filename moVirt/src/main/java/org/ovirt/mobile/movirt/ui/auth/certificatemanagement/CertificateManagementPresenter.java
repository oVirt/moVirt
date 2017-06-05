package org.ovirt.mobile.movirt.ui.auth.certificatemanagement;

import android.content.Context;
import android.net.Uri;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.account.data.CertificateDownloadStatus;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.PropertyChangedListener;
import org.ovirt.mobile.movirt.auth.properties.PropertyUtils;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.property.Cert;
import org.ovirt.mobile.movirt.auth.properties.property.CertHandlingStrategy;
import org.ovirt.mobile.movirt.auth.properties.property.CertLocation;
import org.ovirt.mobile.movirt.ui.auth.certificatemanagement.data.CertContext;
import org.ovirt.mobile.movirt.ui.mvp.AccountListenersDisposablesPresenter;
import org.ovirt.mobile.movirt.util.CertHelper;
import org.ovirt.mobile.movirt.util.URIUtils;
import org.ovirt.mobile.movirt.util.message.ErrorType;
import org.ovirt.mobile.movirt.util.message.MessageHelper;
import org.ovirt.mobile.movirt.util.preferences.CommonSharedPreferencesHelper;

import java.net.URL;
import java.security.cert.Certificate;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@EBean
public class CertificateManagementPresenter extends AccountListenersDisposablesPresenter
        <CertificateManagementPresenter, CertificateManagementContract.View> implements CertificateManagementContract.Presenter {

    private final Subject<CertHandlingStrategy> certHandlingStrategySubject = PublishSubject.create();
    private final Subject<Cert[]> certChainSubject = PublishSubject.create();
    private final Subject<String> validHostnamesSubject = PublishSubject.create();
    private final Subject<CertLocation> customCertificateLocationSubject = PublishSubject.create();

    private MessageHelper messageHelper;

    private AccountPropertiesManager propertiesManager;

    private URL apiUrl;

    private transient String uncheckedApiUrl;

    private int maxVisibleCertificates;

    @Bean
    CommonSharedPreferencesHelper commonSharedPreferencesHelper;

    @Bean
    Resources resources;

    @RootContext
    Context context;

    public CertificateManagementPresenter setApiUrl(String apiUrl) {
        this.uncheckedApiUrl = apiUrl;
        return this;
    }

    public CertificateManagementPresenter setMaxVisibleCertificates(int maxVisibleCertificates) {
        this.maxVisibleCertificates = maxVisibleCertificates;
        return this;
    }

    @Override
    public CertificateManagementPresenter initialize() {
        super.initialize();

        try {
            messageHelper = envStore.getMessageHelper(account);
            propertiesManager = envStore.getAccountPropertiesManager(account);

            try {
                apiUrl = URIUtils.tryToParseUrl(uncheckedApiUrl);
            } catch (IllegalArgumentException x) {
                messageHelper.showToast(resources.validUrlToConfigureCertsError());
                finishSafe();
            }
            getView().displayTitle(account.getName());

            getView().showCertificateDownloadInProgress(envStore.isCertificateDownloadInProgress(account));
            getDisposables().add(rxStore.CERTIFICATE_DOWNLOAD_STATUS
                    .filter(loginStatus -> loginStatus.isAccount(account))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(status -> getView().showCertificateDownloadInProgress(status.isInProgress())));

            getDisposables().add(certHandlingStrategySubject.distinctUntilChanged()
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(certHandlingStrategy -> getView().selectCertHandlingStrategy(certHandlingStrategy)));

            getDisposables().add(Observable.combineLatest(certHandlingStrategySubject, certChainSubject,
                    customCertificateLocationSubject, CertContext::new)
                    .filter(certContext -> certContext.certHandlingStrategy == CertHandlingStrategy.TRUST_CUSTOM)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .distinctUntilChanged()
                    .subscribe(this::onNewCertContext));

            getDisposables().add(customCertificateLocationSubject.distinctUntilChanged()
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(customCertificateLocation -> getView().selectCertLocationType(customCertificateLocation)));

            getDisposables().add(validHostnamesSubject.distinctUntilChanged()
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(hostnames -> getView().showHostnames(hostnames)));

            initPropertyListeners(propertiesManager);
        } catch (AccountDeletedException e) {
            finishSafe();
        }
        return this;
    }

    @Override
    public void destroy() {
        super.destroy();
        certHandlingStrategySubject.onComplete();
        certChainSubject.onComplete();
        validHostnamesSubject.onComplete();
        customCertificateLocationSubject.onComplete();
    }

    private void backgroundOperationStatus(boolean inProgress) {
        rxStore.CERTIFICATE_DOWNLOAD_STATUS.onNext(new CertificateDownloadStatus(account, inProgress));
    }

    @Override
    public void onCertHandlingStrategyChanged(CertHandlingStrategy certHandlingStrategy) {
        propertiesManager.setCertHandlingStrategy(certHandlingStrategy);
    }

    @Override
    public void onValidHostnamesChanged(String hostnames) {
        propertiesManager.setValidHostnameList(PropertyUtils.parseHostnames(hostnames));
    }

    @Override
    @Background
    public void onCertificateLocationChangeAttempt(CertLocation certLocation) {
        try {
            if (propertiesManager.getCertificateChain().length > 0) {
                final CertLocation oldLocation = propertiesManager.getCertificateLocation();
                if (certLocation != oldLocation) {
                    revertCheckFromUiThread(oldLocation, certLocation); // many issues with radio buttons so we call UiThread from Background
                }
            } else {
                propertiesManager.setCertificateLocation(certLocation);
            }
        } catch (AccountDeletedException e) {
            finishSafe();
        }
    }

    @UiThread
    void revertCheckFromUiThread(CertLocation oldLocation, CertLocation certLocation) {
        final CertificateManagementContract.View view = getView();
        if (view != null) {
            view.selectCertLocationType(oldLocation); // switch back
            view.showChangeCertificateLocationDeleteDialog(certLocation); // and ask for
        }
    }

    @Override
    public void downloadDefaultCertificate() {
        try {
            downloadCertificate(null, true);
        } catch (IllegalArgumentException parseError) {
            messageHelper.showError(ErrorType.USER, parseError.getMessage(), resources.wrongUrl());
        }
    }

    @Override
    public void downloadCustomCertificate() {
        try {
            Cert[] certs = propertiesManager.getCertificateChain();

            if (certs.length == 0) {
                getView().showDownloadCustomCaDialog(null, true);
            } else {
                assertCaIncluded(certs, CertLocation.CUSTOM_URL);
            }
        } catch (IllegalArgumentException parseError) {
            messageHelper.showError(ErrorType.USER, parseError.getMessage(), resources.wrongUrl());
        }
    }

    @Background
    @Override
    public void importCustomCertificateFromFile(Uri file) {
        if (file == null) {
            messageHelper.showError(ErrorType.USER, resources.noFileSelected());
            return;
        }

        try {
            backgroundOperationStatus(true);
            final boolean startNew = propertiesManager.getCertificateChain().length == 0;
            CertHelper.loadAndStoreCert(propertiesManager, context, file, apiUrl, startNew);
        } catch (IllegalArgumentException | IllegalStateException e) {
            messageHelper.showError(ErrorType.USER, e.getMessage());
        } catch (AccountDeletedException e) {
            finishSafe();
        } catch (Exception e) {
            messageHelper.showError(ErrorType.USER, resources.invalidApiUrl());
        } finally {
            backgroundOperationStatus(false);
        }
    }

    @Background
    @Override
    public void toggleCustomCertificateLocation(CertLocation certLocation) {
        try {
            backgroundOperationStatus(true);
            CertHelper.deleteAllCerts(propertiesManager); // before cert location
            messageHelper.showToast(resources.deletedCertsChainMessage());
            propertiesManager.setCertificateLocation(certLocation);
        } catch (AccountDeletedException e) {
            finishSafe();
        } finally {
            backgroundOperationStatus(false);
        }
    }

    @Background
    void deleteAllCertsInBackground() {
        try {
            backgroundOperationStatus(true);
            CertHelper.deleteAllCerts(propertiesManager);
            messageHelper.showToast(resources.deletedCertsChainMessage());
        } catch (AccountDeletedException e) {
            finishSafe();
        } finally {
            backgroundOperationStatus(false);
        }
    }

    @Background
    public void downloadCertificate(URL[] urls, boolean startNewChain) {
        if (urls == null) {
            urls = URIUtils.getEngineCertificateUrls(apiUrl);
        }

        try {
            backgroundOperationStatus(true);
            for (int i = 0; i < urls.length; i++) { // try all URLs - used in ENGINE certificate location
                try {
                    CertHelper.downloadAndStoreCert(propertiesManager, urls[i], apiUrl, startNewChain);
                    break;
                } catch (AccountDeletedException e) {
                    finishSafe();
                } catch (Exception x) {
                    if (i == urls.length - 1) {
                        throw x;
                    }
                }
            }
        } catch (AccountDeletedException e) {
            finishSafe();
        } catch (IllegalArgumentException | IllegalStateException e) {
            messageHelper.showError(ErrorType.USER, e.getMessage(), resources.checkedUrlsToString(urls));
        } catch (Exception e) {
            messageHelper.showError(ErrorType.NORMAL, e, resources.checkedUrlsToString(urls));
        } finally {
            backgroundOperationStatus(false);
        }
    }

    private void onNewCertContext(CertContext certContext) {
        Cert[] certs = certContext.certChain;
        boolean hasCerts = certs != null && certs.length > 0;

        if (hasCerts && certs.length > maxVisibleCertificates) {
            messageHelper.showError(ErrorType.USER, resources.getMaxVisibleCertsReachedError(maxVisibleCertificates));
        }

        CertificateManagementContract.View view = getView();
        view.showCustomCertInnerDetailVisibility(hasCerts);
        view.showCerts(null); // clear old certs

        boolean isCA = false;
        if (hasCerts) {
            try {
                isCA = assertCaIncluded(certs, certContext.certificateLocation);
                view.showCerts(certs);
            } catch (Exception x) {
                messageHelper.showError(ErrorType.NORMAL, x, resources.badlyFormattedCertsError());
                deleteAllCertsInBackground();
            }

            if (certContext.certificateLocation == CertLocation.DEFAULT_URL && !isCA) { //engine
                messageHelper.showError(ErrorType.USER, resources.notSelfSignedEngineError());
            }
        }

        view.showDownloadButton(certContext.certificateLocation, !isCA, hasCerts);
    }

    private boolean assertCaIncluded(Cert[] certs, CertLocation certLocation) {
        return !(certs == null || certs.length == 0) && assertCaIncluded(certs[certs.length - 1].asCertificate(), certLocation);
    }

    /**
     * check if Ca is included and show dialog to import issuer certs if isn't
     */
    private boolean assertCaIncluded(Certificate certificate, CertLocation certLocation) {
        boolean isCA = CertHelper.isCA(certificate);
        if (!isCA && !envStore.isCertificateDownloadInProgress(account)) {
            if (certLocation == CertLocation.FILE) {
                getView().showImportFromFileCustomCaDialog();
            } else {
                getView().showDownloadCustomCaDialog(CertHelper.getIssuerUrl(certificate), false);
            }
        }

        return isCA;
    }

    private void initPropertyListeners(AccountPropertiesManager propertiesManager) {
        for (PropertyChangedListener listener : new PropertyChangedListener[]{
                new AccountProperty.CertHandlingStrategyListener() {
                    @Override
                    public void onPropertyChange(CertHandlingStrategy certHandlingStrategy) {
                        certHandlingStrategySubject.onNext(certHandlingStrategy);
                    }
                },
                new AccountProperty.CertificateChainListener() {
                    @Override
                    public void onPropertyChange(Cert[] certificates) {
                        certChainSubject.onNext(certificates);
                    }
                },
                new AccountProperty.ValidHostnamesListener() {
                    @Override
                    public void onPropertyChange(String validHostnames) {
                        validHostnamesSubject.onNext(validHostnames);
                    }
                },
                new AccountProperty.CustomCertificateLocationListener() {
                    @Override
                    public void onPropertyChange(CertLocation certificateLocation) {
                        customCertificateLocationSubject.onNext(certificateLocation);
                    }
                }
        }) {
            getPropertyChangedListeners().add(listener);
            propertiesManager.notifyAndRegisterListener(listener);
        }
    }
}
