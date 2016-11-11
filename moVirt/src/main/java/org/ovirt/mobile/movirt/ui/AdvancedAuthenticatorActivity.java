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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

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
import org.ovirt.mobile.movirt.auth.CaCert;
import org.ovirt.mobile.movirt.auth.properties.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.PropertyChangedListener;
import org.ovirt.mobile.movirt.ui.dialogs.ConfirmDialogFragment;
import org.ovirt.mobile.movirt.util.message.CreateDialogBroadcastReceiver;
import org.ovirt.mobile.movirt.util.message.CreateDialogBroadcastReceiverHelper;
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
    public final static String LOAD_CA_FROM = APP_PACKAGE_DOT + "ui.LOAD_CA_FROM";

    @ViewById
    Spinner certHandlingStrategySpinner;
    @ViewById
    EditText txtCaUrl;
    @ViewById
    EditText txtValidForHostnames;
    @ViewById
    TextView txtCertDetails;
    @ViewById
    Button btnDelete;
    @ViewById
    Button btnLoad;
    @ViewById
    ProgressBar progress;
    @InstanceState
    boolean inProgress;
    @Bean
    MessageHelper messageHelper;
    @Bean
    AccountPropertiesManager propertiesManager;

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
                txtCaUrl.setText("http://" + url.getHost() + "/ovirt-engine/services/pki-resource?resource=ca-certificate&format=X509-PEM-CA");
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
                CaCert[] certs = propertiesManager.getCertificateChain();
                if (certs.length == 1) {
                    CaCert cert = certs[0];
                    if (!cert.getValidFor().equals(s.toString())) { // check here, because it is expensive to check full array in propertiesManager
                        cert.setValidFor(s.toString());
                        propertiesManager.setCertificateChain(certs);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setPropertyListeners() {
        propertiesManager.notifyAndRegisterListener(AccountProperty.CERT_HANDLING_STRATEGY, new PropertyChangedListener<CertHandlingStrategy>() {
            @Override
            public void onPropertyChange(CertHandlingStrategy property) {
                updateViews(property, propertiesManager.getCertificateChain());
            }
        });

        propertiesManager.registerListener(AccountProperty.CERTIFICATE_CHAIN, new PropertyChangedListener<CaCert[]>() {
            @Override
            public void onPropertyChange(CaCert[] property) {
                updateViews(propertiesManager.getCertHandlingStrategy(), property);
            }
        });
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void updateViews(CertHandlingStrategy certHandlingStrategy, CaCert[] caCerts) {
        int id = (int) certHandlingStrategy.id();
        if (certHandlingStrategySpinner.getSelectedItemId() != id) { // check it was not fired by by view listener
            certHandlingStrategySpinner.setSelection((int) certHandlingStrategy.id());
        }

        switch (certHandlingStrategy) {
            case TRUST_CUSTOM:
                seCustomCertVisibility(true);
                if (caCerts.length == 1) {
                    setCertDataToView(caCerts[0]);
                } else {
                    setCertDataToView(null);
                }
                break;
            default:
                seCustomCertVisibility(false);
                setCertDataToView(null);
                break;
        }
    }

    private void seCustomCertVisibility(boolean visible) {
        txtCaUrl.setVisibility(visible ? View.VISIBLE : View.GONE);
        btnDelete.setVisibility(visible ? View.VISIBLE : View.GONE);
        btnLoad.setVisibility(visible ? View.VISIBLE : View.GONE);
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
        propertiesManager.setCertificateChain(new CaCert[]{});
    }

    @Background
    void downloadCert() {
        showProgressBar();
        URL url = null;
        try {
            String endpoint = txtCaUrl.getText().toString();
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
            storeImportedCa(ca, url.getHost());
        } catch (CertificateException e) {
            hideProgressBar();
            showToast("Error CA generation: " + e.getMessage());
            return;
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

    void setCertDataToView(CaCert cert) {
        boolean visible = cert != null;

        String caValue = visible ? cert.asCertificate().toString() : "";
        String urlsValue = visible ? cert.getValidFor() : "";

        if (btnDelete != null) {
            btnDelete.setEnabled(visible);
        }

        if (txtCertDetails != null) {
            txtCertDetails.setVisibility(visible ? View.VISIBLE : View.GONE);
            if (visible && !txtCertDetails.getText().equals(caValue)) { // ignore setter when set to null && check it was not fired by by view listener
                txtCertDetails.setText(caValue);
            }
        }

        if (txtValidForHostnames != null) {
            txtValidForHostnames.setVisibility(visible ? View.VISIBLE : View.GONE);
            if (visible && !txtValidForHostnames.getText().toString().equals(urlsValue)) { // same
                txtValidForHostnames.setText(urlsValue);
            }
        }
    }

    boolean storeImportedCa(Certificate ca, String validFor) {
        if (ca == null) {
            return false;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            try {
                out = new ObjectOutputStream(bos);
                out.writeObject(ca);
                byte[] caAsBlob = bos.toByteArray();
                CaCert caCert = new CaCert();
                caCert.setContent(caAsBlob);
                caCert.setValidFor(validFor);
                propertiesManager.setCertificateChain(new CaCert[]{caCert}); // current thread is already background
            } catch (IOException e) {
                e.printStackTrace();
                showToast("Error storing certificate: " + e.getMessage());
                return false;
            }
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
            try {
                bos.close();
            } catch (IOException ignore) {
            }
        }

        // if the output stream is not closed successfully the cert is still stored well
        return true;
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
