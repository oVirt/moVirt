package org.ovirt.mobile.movirt.ui.auth;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.PropertyChangedListener;
import org.ovirt.mobile.movirt.auth.properties.PropertyUtils;
import org.ovirt.mobile.movirt.auth.properties.UiAwareProperty;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.manager.OnThread;
import org.ovirt.mobile.movirt.auth.properties.property.Cert;
import org.ovirt.mobile.movirt.auth.properties.property.CertHandlingStrategy;
import org.ovirt.mobile.movirt.ui.dialogs.ConfirmDialogFragment;
import org.ovirt.mobile.movirt.ui.dialogs.DialogListener;
import org.ovirt.mobile.movirt.util.CertHelper;
import org.ovirt.mobile.movirt.util.URIUtils;
import org.ovirt.mobile.movirt.util.message.CreateDialogBroadcastReceiver;
import org.ovirt.mobile.movirt.util.message.CreateDialogBroadcastReceiverHelper;
import org.ovirt.mobile.movirt.util.message.ErrorType;
import org.ovirt.mobile.movirt.util.message.MessageHelper;

import java.net.URL;
import java.security.cert.Certificate;

import static org.ovirt.mobile.movirt.Constants.APP_PACKAGE_DOT;

@EActivity(R.layout.activity_advanced_authenticator)
public class AdvancedAuthenticatorActivity extends ActionBarActivity
        implements ConfirmDialogFragment.ConfirmDialogListener, CreateDialogBroadcastReceiver, DialogListener.UrlListener {
    private static final String TAG = AdvancedAuthenticatorActivity.class.getSimpleName();

    private static final String CUSTOM_DIALOG_TAG = "downloadCaIssuer";
    private static final String DELETE_DIALOG_TAG = "deleteCert";
    private static final String DELETE_DIALOG_FROM_CUSTOM_LOCATION_LISTENER_TAG = "deleteCertFromCustomLocationListener";

    private static final int NORMAL_DELETE_DIALOG = 0;
    private static final int CUSTOM_SWITCH_DELETE_DIALOG = 1;
    private static final int MAX_VISIBLE_CERTIFICATES = 10;

    public final static String LOAD_CA_FROM = APP_PACKAGE_DOT + "ui.LOAD_CA_FROM";

    @ViewById
    Spinner certHandlingStrategySpinner;

    @ViewById
    Switch customCertificateLocationSwitch;

    @ViewById
    TextView txtCertUrl;

    @ViewById
    EditText txtValidForHostnames;

    @ViewById
    TextView txtCertDetails;

    @ViewById
    TextView certUrlLabel;

    @ViewById
    TextView validHostnamesLabel;

    @ViewById
    Button btnDelete;

    @ViewById
    Button btnLoad;

    @ViewById
    ProgressBar progress;

    @ViewById
    LinearLayout certTreeContainer;

    @InstanceState
    boolean inProgress;

    private boolean initializedUi;

    @Bean
    MessageHelper messageHelper;

    @Bean
    AccountPropertiesManager propertiesManager;

    @Bean
    CertHelper certHelper;

    private PropertyChangedListener[] listeners;

    private boolean maxCertsReachedErrorAlreadyShown;

    @AfterViews
    void init() {
        showProgressBar(inProgress);
        initViewListeners();
        initPropertyListeners();
    }

    @Override
    protected void onDestroy() {
        for (PropertyChangedListener listener : listeners) {
            propertiesManager.removeListener(listener);
        }
        super.onDestroy();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void updateViews(UiAwareProperty<CertHandlingStrategy> certHandlingStrategy, UiAwareProperty<Cert[]> certs,
                     UiAwareProperty<String> hostnames, UiAwareProperty<Boolean> isCustomCertificateLocation) {
        updateCertHandlingStrategy(certHandlingStrategy);

        switch (certHandlingStrategy.getProperty()) {
            case TRUST_CUSTOM:
                seCustomCertVisibility(true);

                updateHostnames(hostnames);
                updateCertificateLocationType(isCustomCertificateLocation);

                Cert[] chain = certs.getProperty();
                assertMaxCertsMessage(chain);
                showCerts(chain, isCustomCertificateLocation.getProperty());
                break;
            default:
                seCustomCertVisibility(false);

                hideCerts();
                break;
        }
    }

    @Click(R.id.btnLoad)
    void btnDownLoad() {
        try {
            if (propertiesManager.isCustomCertificateLocation()) {
                Cert[] certs = propertiesManager.getCertificateChain();

                if (certs.length == 0) {
                    downloadCustomCa(null, true);
                } else {
                    assertCaIncluded(certs);
                }
            } else {
                downloadAndSaveCert(null, true);
            }
        } catch (IllegalArgumentException parseError) {
            messageHelper.showError(ErrorType.USER, parseError.getMessage(), getString(R.string.wrong_url_in_connection_settings));
        }
    }

    @Override
    public void onNewDialogUrl(URL url, boolean startNewChain) {
        downloadAndSaveCert(new URL[]{url}, startNewChain);
    }

    @Background
    void downloadAndSaveCert(URL[] urls, boolean startNewChain) {
        URL hostUrl = null;
        try {
            hostUrl = URIUtils.tryToParseUrl(getIntent().getStringExtra(LOAD_CA_FROM));
        } catch (Exception x) {
            messageHelper.showError(ErrorType.USER, getString(R.string.api_url_not_valid));
            return;
        }
        if (urls == null) {
            urls = URIUtils.getEngineCertificateUrls(hostUrl);
        }

        try {
            broadcastProgress(true);
            for (int i = 0; i < urls.length; i++) { // try all URLs - used in ENGINE certificate location
                try {
                    certHelper.downloadAndStoreCert(urls[i], hostUrl, startNewChain);
                    break;
                } catch (Exception x) {
                    if (i == urls.length - 1) {
                        throw x;
                    }
                }
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            messageHelper.showError(ErrorType.USER, e.getMessage(), checkedUrlsToString(urls));
        } catch (Exception e) {
            messageHelper.showError(ErrorType.NORMAL, e, checkedUrlsToString(urls));
        } finally {
            broadcastProgress(false);
        }
    }

    @Click(R.id.btnDelete)
    void btnDelete() {
        DialogFragment confirmDialog = ConfirmDialogFragment
                .newInstance(NORMAL_DELETE_DIALOG, getString(R.string.dialog_action_delete_certificate));
        confirmDialog.show(getFragmentManager(), DELETE_DIALOG_TAG);
    }

    @Override
    public void onDialogResult(int dialogButton, int actionId) {
        if (dialogButton == DialogInterface.BUTTON_POSITIVE) {
            if (actionId == CUSTOM_SWITCH_DELETE_DIALOG) { // toggle switch
                boolean isChecked = customCertificateLocationSwitch.isChecked();
                changeCertificateLocation(isChecked);
            } else {
                deleteAllCertsInBackground();
            }
        }
    }

    private void deleteAllCertsInBackground() {
        certHelper.deleteAllCertsInBackground();
        messageHelper.showToast(getString(R.string.deleted_cert_chain));
    }

    @Background
    void changeCertificateLocation(boolean customCertificateChecked) {
        certHelper.deleteAllCerts(); // before cert location
        messageHelper.showToast(getString(R.string.deleted_cert_chain));
        propertiesManager.setCustomCertificateLocation(!customCertificateChecked, OnThread.BACKGROUND);
    }

    private void downloadCustomCa(String url, boolean startNewChain) {
        if (getFragmentManager().findFragmentByTag(CUSTOM_DIALOG_TAG) == null) { // updateViews can call this multiple times
            DownloadCustomCertDialogFragment dialog = new DownloadCustomCertDialogFragment_();
            dialog.setUrl(url);
            dialog.setStartNewChain(startNewChain);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(dialog, CUSTOM_DIALOG_TAG);
            transaction.commitAllowingStateLoss();
        }
    }

    private static String checkedUrlsToString(URL[] urls) {
        StringBuilder urlsChecked = new StringBuilder("Urls checked:\n");
        for (int u = 0; u < urls.length; u++) {
            urlsChecked.append(urls[u]);
            if (u != urls.length - 1) {
                urlsChecked.append(",\n");
            }
        }
        urlsChecked.append("\n");
        return urlsChecked.toString();
    }

    private boolean assertCaIncluded(Cert[] certs) {
        return !(certs == null || certs.length == 0) && assertCaIncluded(certs[certs.length - 1].asCertificate());
    }

    /**
     * check if Ca is included and show dialog to import issuer certs if isn't
     */
    private boolean assertCaIncluded(Certificate certificate) {
        boolean isCA = CertHelper.isCA(certificate);
        if (!isCA) {
            downloadCustomCa(CertHelper.getIssuerUrl(certificate), false);
        }

        return isCA;
    }

    // update view functions
    private void updateCertHandlingStrategy(UiAwareProperty<CertHandlingStrategy> certHandlingStrategy) {
        if (certHandlingStrategy.uiNotUpdated()) {
            certHandlingStrategySpinner.setSelection((int) certHandlingStrategy.getProperty().id());
        }
    }

    private void assertMaxCertsMessage(Cert[] certs) {
        if (certs.length > MAX_VISIBLE_CERTIFICATES) {
            maxCertsReachedErrorAlreadyShown = false;
        }
    }

    private void updateHostnames(UiAwareProperty<String> hostnames) {
        if (hostnames.uiNotUpdated()) {
            txtValidForHostnames.setText(hostnames.getProperty());
        }
    }

    private void updateCertificateLocationType(UiAwareProperty<Boolean> isCustomCertificateLocation) {
        if (isCustomCertificateLocation.uiNotUpdated()) {
            customCertificateLocationSwitch.setChecked(isCustomCertificateLocation.getProperty());
        }
    }

    private void hideCerts() {
        showCerts(null, null);
    }

    private void showCerts(Cert[] certs, Boolean isCustomCert) {
        boolean hasCerts = certs != null && certs.length > 0;

        setHasCertsVisibility(hasCerts);
        certTreeContainer.removeAllViews(); // remove previous tree
        boolean isCA = false;
        if (hasCerts) {
            try {
                isCA = assertCaIncluded(certs);
                createTreeView(certTreeContainer, certs);
            } catch (Exception x) {
                messageHelper.showError(ErrorType.NORMAL, x, "Certificates badly formatted");
                deleteAllCertsInBackground();
            }

            if (!isCustomCert && !isCA) { //engine
                messageHelper.showError(ErrorType.USER, "Engine doesn't use self-signed CA, please report this issue.");
            }
        }

        if (isCustomCert != null) { // null -> already hidden
            setLoadButton(isCA, isCustomCert, hasCerts);
        }
    }

    private void seCustomCertVisibility(boolean visible) {
        btnDelete.setVisibility(visible ? View.VISIBLE : View.GONE);
        btnLoad.setVisibility(visible ? View.VISIBLE : View.GONE);
        customCertificateLocationSwitch.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setLoadButton(boolean isCA, boolean isCustomCert, boolean hasCerts) {
        boolean visible = !isCustomCert || !isCA; // show allways for engine and for custom without CA

        btnLoad.setVisibility(visible ? View.VISIBLE : View.GONE);
        btnLoad.setText(isCustomCert && hasCerts ? R.string.download_issuer : R.string.download);
    }

    private void setHasCertsVisibility(boolean hasCerts) {
        btnDelete.setEnabled(hasCerts);
        txtValidForHostnames.setVisibility(hasCerts ? View.VISIBLE : View.GONE);
        validHostnamesLabel.setVisibility(hasCerts ? View.VISIBLE : View.GONE);

        txtCertUrl.setVisibility(hasCerts ? View.VISIBLE : View.GONE);
        certUrlLabel.setVisibility(hasCerts ? View.VISIBLE : View.GONE);

        txtCertDetails.setVisibility(hasCerts ? View.VISIBLE : View.GONE);
        txtCertDetails.setText(null);

        certTreeContainer.setVisibility(hasCerts ? View.VISIBLE : View.GONE);
    }

    private void createTreeView(LinearLayout container, Cert[] certs) {
        TreeNode root = TreeNode.root();
        TreeNode intermediateLeaf = root;
        CertHolder leafHolder = null;
        int visibleCertificates;

        if (certs.length > MAX_VISIBLE_CERTIFICATES) {
            visibleCertificates = MAX_VISIBLE_CERTIFICATES;
            if (!maxCertsReachedErrorAlreadyShown) {
                maxCertsReachedErrorAlreadyShown = true;
                messageHelper.showError(ErrorType.USER, getString(R.string.advanced_authenticator_error_max_visible_certs_reached, MAX_VISIBLE_CERTIFICATES));
            }
        } else {
            visibleCertificates = certs.length;
        }

        CertHolder.CertificateSelectedListener listener = new CertHolder.CertificateSelectedListener() {
            @Override
            public void onSelect(Certificate certificate, String location) {
                txtCertDetails.setText(certificate.toString());
                txtCertUrl.setText(location);
            }
        };

        for (int i = visibleCertificates - 1; i >= 0; i--) { // create tree hierarchy
            CertHolder.TreeItem data = new CertHolder.TreeItem(certs[i], listener);
            leafHolder = new CertHolder(this);
            TreeNode newNode = new TreeNode(data).setViewHolder(leafHolder);

            intermediateLeaf.addChild(newNode);
            intermediateLeaf.setExpanded(true);
            intermediateLeaf = newNode;
        }

        AndroidTreeView atv = new AndroidTreeView(this, root);
        atv.setUseAutoToggle(false);
        atv.setSelectionModeEnabled(true);
        atv.setDefaultContainerStyle(R.style.CertTreeNodeStyle);
        container.addView(atv.getView());
        if (leafHolder != null) {
            leafHolder.selectNode(); // select Api certificate
        }
    }

    // Data Flow and listeners
    private void initViewListeners() {
        certHandlingStrategySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                propertiesManager.setCertHandlingStrategy(CertHandlingStrategy.from(id),
                        OnThread.BACKGROUND);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        txtValidForHostnames.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                propertiesManager.setValidHostnameList(PropertyUtils.parseHostnames(s.toString()),
                        OnThread.BACKGROUND);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        customCertificateLocationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (propertiesManager.getCertificateChain().length > 0) {
                    customCertificateLocationSwitch.setChecked(propertiesManager.isCustomCertificateLocation());
                    // delete certs before switching mode
                    DialogFragment confirmDialog = ConfirmDialogFragment
                            .newInstance(CUSTOM_SWITCH_DELETE_DIALOG, getString(R.string.dialog_action_delete_certificate),
                                    getString(R.string.dialog_action_delete_certificate_header));
                    confirmDialog.show(getFragmentManager(), DELETE_DIALOG_FROM_CUSTOM_LOCATION_LISTENER_TAG);
                } else {
                    propertiesManager.setCustomCertificateLocation(!propertiesManager.isCustomCertificateLocation(),
                            OnThread.BACKGROUND);
                }
            }
        });
    }

    private void initPropertyListeners() {
        AccountProperty.CertHandlingStrategyListener certHandlingStrategyListener = new AccountProperty.CertHandlingStrategyListener() {
            @Override
            public void onPropertyChange(CertHandlingStrategy certHandlingStrategy) {
                prepareDataAndUpdateViews(certHandlingStrategy, null, null, null);
            }
        };

        AccountProperty.CertificateChainListener certChainListener = new AccountProperty.CertificateChainListener() {
            @Override
            public void onPropertyChange(Cert[] certificates) {
                prepareDataAndUpdateViews(null, certificates, null, null);
            }
        };

        AccountProperty.ValidHostnamesListener validHostnamesListener = new AccountProperty.ValidHostnamesListener() {
            @Override
            public void onPropertyChange(String validHostnames) {
                prepareDataAndUpdateViews(null, null, validHostnames, null);
            }
        };

        AccountProperty.CustomCertificateLocationListener customCertificateLocationListener = new AccountProperty.CustomCertificateLocationListener() {
            @Override
            public void onPropertyChange(Boolean customCertificateLocation) {
                prepareDataAndUpdateViews(null, null, null, customCertificateLocation);
            }
        };

        listeners = new PropertyChangedListener[]{
                certHandlingStrategyListener,
                certChainListener,
                validHostnamesListener,
                customCertificateLocationListener};

        propertiesManager.notifyAndRegisterListener(certHandlingStrategyListener);
        propertiesManager.registerListener(certChainListener);
        propertiesManager.registerListener(validHostnamesListener);
        propertiesManager.registerListener(customCertificateLocationListener);
    }

    private void prepareDataAndUpdateViews(CertHandlingStrategy certHandlingStrategy, Cert[] certs, String hostnames, Boolean isCustomCertificateLocation) {
        boolean initialized = initializedUi; //first time propagate all properties to UI
        if (!initializedUi) {
            initializedUi = true;
        }

        // fill empty properties
        if (certHandlingStrategy == null) {
            certHandlingStrategy = propertiesManager.getCertHandlingStrategy();
        }

        if (certs == null) {
            certs = propertiesManager.getCertificateChain();
        }

        if (hostnames == null) {
            hostnames = propertiesManager.getValidHostnames();
        }

        if (isCustomCertificateLocation == null) {
            isCustomCertificateLocation = propertiesManager.isCustomCertificateLocation();
        }

        // set which changes were updated from this UI
        CertHandlingStrategy oldStrategy = initialized ? CertHandlingStrategy.from(certHandlingStrategySpinner.getSelectedItemId()) : null;
        UiAwareProperty<CertHandlingStrategy> uiStrategy = new UiAwareProperty<>(certHandlingStrategy, oldStrategy);

        String oldHostnames = initialized ? PropertyUtils.catenateToCsv(PropertyUtils.parseHostnames(txtValidForHostnames.getText().toString())) : null;
        UiAwareProperty<String> uiHostnames = new UiAwareProperty<>(hostnames, oldHostnames);

        Boolean oldLocation = initialized ? customCertificateLocationSwitch.isChecked() : null;

        UiAwareProperty<Boolean> uiLocation = new UiAwareProperty<>(isCustomCertificateLocation, oldLocation);

        UiAwareProperty<Cert[]> uiCerts = new UiAwareProperty<>(certs); // doesn't depend on UI

        updateViews(uiStrategy, uiCerts, uiHostnames, uiLocation);
    }

    @OptionsItem(android.R.id.home)
    public void homeSelected() {
        finish();
    }

    void broadcastProgress(boolean inProgress) {
        Intent intent = new Intent(Broadcasts.DOWNLOADING_CERTIFICATE);
        intent.putExtra(Broadcasts.Extras.IN_PROGRESS, inProgress);
        getApplicationContext().sendBroadcast(intent);
    }

    @Receiver(actions = {Broadcasts.DOWNLOADING_CERTIFICATE},
            registerAt = Receiver.RegisterAt.OnCreateOnDestroy)
    void showProgressBar(@Receiver.Extra(Broadcasts.Extras.IN_PROGRESS) boolean inProgress) {
        this.inProgress = inProgress;
        if (progress != null) {
            progress.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        }
    }

    @Receiver(actions = {Broadcasts.ERROR_MESSAGE},
            registerAt = Receiver.RegisterAt.OnResumeOnPause)
    public void showErrorDialog(
            @Receiver.Extra(Broadcasts.Extras.ERROR_REASON) String reason,
            @Receiver.Extra(Broadcasts.Extras.REPEATED_MINOR_ERROR) boolean repeatedMinorError) {
        CreateDialogBroadcastReceiverHelper.showErrorDialog(getFragmentManager(), reason, repeatedMinorError);
    }

    @Receiver(actions = {Broadcasts.REST_CA_FAILURE},
            registerAt = Receiver.RegisterAt.OnResumeOnPause)
    public void showCertificateDialog(
            @Receiver.Extra(Broadcasts.Extras.ERROR_REASON) String reason) {
        // Do not use CreateDialogBroadcastReceiverHelper implementation, coz we are already setting certificate in this Activity
        messageHelper.showError(getString(R.string.dialog_certificate_missing_start));
    }
}
