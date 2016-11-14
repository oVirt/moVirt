package org.ovirt.mobile.movirt.ui;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import org.ovirt.mobile.movirt.auth.Cert;
import org.ovirt.mobile.movirt.auth.properties.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.ParseUtils;
import org.ovirt.mobile.movirt.auth.properties.PropertyChangedListener;
import org.ovirt.mobile.movirt.ui.dialogs.ConfirmDialogFragment;
import org.ovirt.mobile.movirt.util.message.CreateDialogBroadcastReceiver;
import org.ovirt.mobile.movirt.util.message.CreateDialogBroadcastReceiverHelper;
import org.ovirt.mobile.movirt.util.message.ErrorType;
import org.ovirt.mobile.movirt.util.message.MessageHelper;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import static org.ovirt.mobile.movirt.Constants.APP_PACKAGE_DOT;

@EActivity(R.layout.activity_advanced_authenticator)
public class AdvancedAuthenticatorActivity extends ActionBarActivity
        implements ConfirmDialogFragment.ConfirmDialogListener, CreateDialogBroadcastReceiver {
    private static final int MAX_VISIBLE_CERTIFICATES = 10;

    public final static String LOAD_CA_FROM = APP_PACKAGE_DOT + "ui.LOAD_CA_FROM";

    @ViewById
    Spinner certHandlingStrategySpinner;

    @ViewById
    EditText txtCertUrl;

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

    @Bean
    MessageHelper messageHelper;

    @Bean
    AccountPropertiesManager propertiesManager;

    private PropertyChangedListener[] listeners;

    private boolean maxCertsReachedErrorAlreadyShown;

    @AfterViews
    void init() {
        // when turning the device
        if (inProgress) {
            showProgressBar();
        } else {
            hideProgressBar();
        }

        String endpoint = getIntent().getStringExtra(LOAD_CA_FROM);
        if (!TextUtils.isEmpty(endpoint)) {
            try {
                URL url = new URL(endpoint);
                txtCertUrl.setText("http://" + url.getHost() + "/ovirt-engine/services/pki-resource?resource=ca-certificate&format=X509-PEM-CA");
            } catch (MalformedURLException e) {
                // no problem - just a convenience to help the most common case
            }
        }

        setViewListeners();
        setPropertyListeners();
    }

    private void setViewListeners() {
        certHandlingStrategySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                propertiesManager.setCertHandlingStrategy(CertHandlingStrategy.from(id), AccountPropertiesManager.OnThread.BACKGROUND);
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
                propertiesManager.setValidHostnameList(ParseUtils.parseHostnames(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setPropertyListeners() {
        PropertyChangedListener certHandlingStrategyListener = new PropertyChangedListener<CertHandlingStrategy>() {
            @Override
            public void onPropertyChange(CertHandlingStrategy property) {
                updateViews(property, null);
            }
        };
        PropertyChangedListener validHostnamesListener = new PropertyChangedListener<String>() {
            @Override
            public void onPropertyChange(String property) {
                updateHostnames(property);
            }
        };
        PropertyChangedListener certChainListener = new PropertyChangedListener<Cert[]>() {
            @Override
            public void onPropertyChange(Cert[] property) {
                if (property.length > MAX_VISIBLE_CERTIFICATES) {
                    maxCertsReachedErrorAlreadyShown = false;
                }
                updateViews(propertiesManager.getCertHandlingStrategy(), property);
            }
        };

        listeners = new PropertyChangedListener[]{certHandlingStrategyListener, validHostnamesListener, certChainListener};

        propertiesManager.notifyAndRegisterListener(AccountProperty.CERT_HANDLING_STRATEGY, certHandlingStrategyListener);
        propertiesManager.notifyAndRegisterListener(AccountProperty.VALID_HOSTNAMES, validHostnamesListener);
        propertiesManager.registerListener(AccountProperty.CERTIFICATE_CHAIN, certChainListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (PropertyChangedListener listener : listeners) {
            propertiesManager.removeListener(listener);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void updateViews(CertHandlingStrategy certHandlingStrategy, Cert[] certs) {
        updateCertHandlingStrategy(certHandlingStrategy);

        switch (certHandlingStrategy) {
            case TRUST_CUSTOM:
                seCustomCertVisibility(true);
                if (certs == null) {
                    certs = propertiesManager.getCertificateChain();
                }
                showCerts(certs);
                break;
            default:
                seCustomCertVisibility(false);
                hideCerts();
                break;
        }
    }

    private void seCustomCertVisibility(boolean visible) {
        txtCertUrl.setVisibility(visible ? View.VISIBLE : View.GONE);
        certUrlLabel.setVisibility(visible ? View.VISIBLE : View.GONE);
        btnDelete.setVisibility(visible ? View.VISIBLE : View.GONE);
        btnLoad.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void updateCertHandlingStrategy(CertHandlingStrategy certHandlingStrategy) {
        int id = (int) certHandlingStrategy.id();
        if (certHandlingStrategySpinner.getSelectedItemId() != id) { // check it was not fired by by view listener
            certHandlingStrategySpinner.setSelection((int) certHandlingStrategy.id());
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void updateHostnames(String hostnames) {
        boolean propertyDiffers = propertiesManager.propertyDiffers(AccountProperty.VALID_HOSTNAME_LIST,
                ParseUtils.parseHostnames(txtValidForHostnames.getText().toString()));
        if (propertyDiffers) {  // check if it is different after parsing
            txtValidForHostnames.setText(hostnames);
        }
    }

    private void hideCerts() {
        showCerts(null);
    }

    private void showCerts(Cert[] certs) {
        boolean visible = certs != null && certs.length > 0;

        if (btnDelete != null) {
            btnDelete.setEnabled(visible);
        }

        if (txtValidForHostnames != null) {
            txtValidForHostnames.setVisibility(visible ? View.VISIBLE : View.GONE);
            validHostnamesLabel.setVisibility(visible ? View.VISIBLE : View.GONE);
        }

        if (txtCertDetails != null) {
            txtCertDetails.setVisibility(visible ? View.VISIBLE : View.GONE);
            txtCertDetails.setText(null);
        }

        if (certTreeContainer != null) {
            certTreeContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
            certTreeContainer.removeAllViews(); // remove previous tree
            if (visible) {
                createTreeView(certs);
            }
        }
    }

    private void createTreeView(Cert[] certs) {
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
            public void onSelect(Certificate certificate) {
                txtCertDetails.setText(certificate.toString());
            }
        };

        for (int i = visibleCertificates - 1; i >= 0; i--) { // create tree hierarchy
            CertHolder.TreeItem data = new CertHolder.TreeItem(certs[i].asCertificate(), listener);
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
        certTreeContainer.addView(atv.getView());
        if (leafHolder != null) {
            leafHolder.selectNode(); // select Api certificate
        }
    }

    @Click(R.id.btnDelete)
    void btnDelete() {
        DialogFragment confirmDialog = ConfirmDialogFragment
                .newInstance(0, getString(R.string.dialog_action_delete_certificate));
        confirmDialog.show(getFragmentManager(), "confirmDeleteCert");
    }

    @Override
    public void onDialogResult(int dialogButton, int actionId) {
        if (dialogButton == DialogInterface.BUTTON_POSITIVE) {
            deleteAllCerts();
        }
    }

    @Click(R.id.btnLoad)
    void btnLoad() {
        downloadCert();
    }

    @Background
    void deleteAllCerts() {
        propertiesManager.setCertificateChain(new Cert[]{});
        propertiesManager.setValidHostnameList(new String[]{});
    }

    @Background
    void downloadCert() {
        showProgressBar();
        URL url = null;
        try {
            String endpoint = txtCertUrl.getText().toString();
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            hideProgressBar();
            showToast("The endpoint is not a valid URL");
            return;
        }

        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            hideProgressBar();
            showToast("Problem getting the certificate factory: " + e.getMessage());
            return;
        }

        InputStream caInput = null;
        ByteArrayOutputStream caOutput = null;

        try {
            caInput = new BufferedInputStream(url.openStream());

            caOutput = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = caInput.read(buffer, 0, buffer.length)) != -1) {
                caOutput.write(buffer, 0, len);
            }
            caOutput.flush();

            caInput = new ByteArrayInputStream(caOutput.toByteArray());
        } catch (IOException e) {
            hideProgressBar();
            showToast("Error loading certificate: " + e.getMessage());
            return;
        }

        try {
            Certificate ca = cf.generateCertificate(caInput);
            propertiesManager.setValidHostnameList(new String[]{url.getHost()});
            storeImportedCa(ca);
        } catch (Exception e) {
            hideProgressBar();
            deleteAllCerts(); // hostname and ca must be atomic
            messageHelper.showError(ErrorType.NORMAL, e, "Error storing certificate");
        } finally {
            try {
                caInput.close();
                caOutput.close();
            } catch (IOException e) {
                // really nothing to do about this one...
            }
            hideProgressBar();
        }
    }

    private void storeImportedCa(Certificate certificate) throws Exception {
        if (certificate == null) {
            throw new IllegalArgumentException("Certificate is null");
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(certificate);
            byte[] caAsBlob = bos.toByteArray();
            Cert cert = new Cert();
            cert.setContent(caAsBlob);
            propertiesManager.setCertificateChain(new Cert[]{cert}); // current thread is already background
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } finally {
                bos.close();
            }
        }
    }

    void showToast(String msg) {
        messageHelper.showToast(msg);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showProgressBar() {
        inProgress = true;
        if (progress != null) {
            progress.setVisibility(View.VISIBLE);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void hideProgressBar() {
        inProgress = false;
        if (progress != null) {
            progress.setVisibility(View.GONE);
        }
    }

    @OptionsItem(android.R.id.home)
    public void homeSelected() {
        finish();
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
