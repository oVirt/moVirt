package org.ovirt.mobile.movirt.ui;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.CaCert;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.dialogs.ConfirmDialogFragment;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;

@EActivity(R.layout.activity_advanced_authenticator)
@OptionsMenu(R.menu.advanced_authenticator)
public class AdvancedAuthenticatorActivity extends ActionBarActivity
        implements ConfirmDialogFragment.ConfirmDialogListener {

    public final static String CERT_HANDLING_STRATEGY = "org.ovirt.mobile.movirt.ui.CERT_HANDLING_STRATEGY";
    public final static String LOAD_CA_FROM = "org.ovirt.mobile.movirt.ui.LOAD_CA_FROM";
    public final static String MODE = "org.ovirt.mobile.movirt.ui.MODE";
    public final static int MODE_REST_CA_MANAGEMENT = 1;
    public final static int MODE_SPICE_CA_MANAGEMENT = 2;
    @Bean
    ProviderFacade providerFacade;
    @ViewById
    Spinner certHandlingStrategy;
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
    @InstanceState
    Certificate certificate;
    @ViewById
    ProgressBar progress;
    @InstanceState
    boolean inProgress;
    @InstanceState
    byte[] caCert;
    @InstanceState
    int mode;
    @OptionsMenuItem(R.id.action_rest_ca_management)
    MenuItem menuRestCaManagement;
    @OptionsMenuItem(R.id.action_spice_ca_management)
    MenuItem menuSpiceCaManagement;

    @AfterViews
    void init() {
        // when turning the device
        if (inProgress) {
            showProgressBar();
        } else {
            hideProgressBar();
        }

        if (mode == 0) {
            mode = getIntent().getIntExtra(MODE, MODE_REST_CA_MANAGEMENT);
        }

        certHandlingStrategy.setOnItemSelectedListener(new CertHandlingSelectionChanged());

        long handlingStrategyId = getIntent().getLongExtra(CERT_HANDLING_STRATEGY, CertHandlingStrategy.TRUST_SYSTEM.id());
        certHandlingStrategy.setSelection((int) handlingStrategyId);

        String endpoint = getIntent().getStringExtra(LOAD_CA_FROM);
        if (!TextUtils.isEmpty(endpoint)) {
            try {
                URL url = new URL(endpoint);
                txtCaUrl.setText("http://" + url.getHost() + "/ovirt-engine/services/pki-resource?resource=ca-certificate&format=X509-PEM-CA");
            } catch (MalformedURLException e) {
                // no problem - just a convenience to help the most common case
            }
        }

        updateViews();
    }

    private void updateViews() {
        switch (mode) {
            case MODE_REST_CA_MANAGEMENT:
                certHandlingStrategy.setVisibility(View.VISIBLE);
                setCertDataToView(null, null);

                if (CertHandlingStrategy.from(certHandlingStrategy.getSelectedItemId()) == CertHandlingStrategy.TRUST_CUSTOM) {
                    setVisibilityForCaViews(true);

                    Collection<CaCert> caCerts = providerFacade.query(CaCert.class).all();
                    if (caCerts.size() == 1) {
                        CaCert trustedCert = caCerts.iterator().next();

                        setCertDataToView(trustedCert.asCertificate(), trustedCert.getValidFor());
                        certificate = trustedCert.asCertificate();

                        btnDelete.setEnabled(true);
                    } else {
                        btnDelete.setEnabled(false);
                    }
                } else {
                    setVisibilityForCaViews(false);
                }

                setTitle(R.string.rest_ca_management);

                break;
            case MODE_SPICE_CA_MANAGEMENT:
                certHandlingStrategy.setVisibility(View.GONE);
                setVisibilityForCaViews(true);
                txtValidForHostnames.setVisibility(View.GONE);
                setCertDataToView(null, null);

                setTitle(R.string.spice_ca_management);

                if (isCaFileExists()) {
                    btnDelete.setEnabled(true);
                } else {
                    btnDelete.setEnabled(false);
                }

                break;
            default:
                break;
        }
    }

    private void setVisibilityForCaViews(boolean visible) {
        txtCaUrl.setVisibility(visible ? View.VISIBLE : View.GONE);
        txtValidForHostnames.setVisibility(visible ? View.VISIBLE : View.GONE);
        txtCertDetails.setVisibility(visible ? View.VISIBLE : View.GONE);
        btnDelete.setVisibility(visible ? View.VISIBLE : View.GONE);
        btnLoad.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private boolean isCaFileExists() {
        File file = new File(Constants.getCaCertPath());
        return file.exists();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        updateMenuItem();
        return super.onCreateOptionsMenu(menu);
    }

    private void updateMenuItem() {
        switch (mode) {
            case MODE_REST_CA_MANAGEMENT:
                if (menuRestCaManagement != null && menuSpiceCaManagement != null) {
                    menuRestCaManagement.setVisible(false);
                    menuSpiceCaManagement.setVisible(true);
                }
                break;
            case MODE_SPICE_CA_MANAGEMENT:
                if (menuRestCaManagement != null && menuSpiceCaManagement != null) {
                    menuRestCaManagement.setVisible(true);
                    menuSpiceCaManagement.setVisible(false);
                }
                break;
            default:
                break;
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
            switch (mode) {
                case MODE_REST_CA_MANAGEMENT:
                    deleteAllCaCerts();
                    certificate = null;
                    certHandlingStrategy.setSelection((int) CertHandlingStrategy.TRUST_SYSTEM.id());
                    setCertDataToView(null, null);
                    break;
                case MODE_SPICE_CA_MANAGEMENT:
                    deleteCaFile();
                    setCertDataToView(null, null);
                    break;
                default:
                    break;
            }

            btnDelete.setEnabled(false);
        }
    }

    @Click(R.id.btnSave)
    void btnSave() {
        switch (mode) {
            case MODE_REST_CA_MANAGEMENT:
                Intent response = new Intent();

                if (CertHandlingStrategy.from(certHandlingStrategy.getSelectedItemId()) == CertHandlingStrategy.TRUST_CUSTOM) {
                    if (storeImportedCa()) {
                        response.putExtra(CERT_HANDLING_STRATEGY, certHandlingStrategy.getSelectedItemId());
                    } else {
                        // not stored - fallback to trust system certs only
                        response.putExtra(CERT_HANDLING_STRATEGY, CertHandlingStrategy.TRUST_SYSTEM.id());
                    }
                } else {
                    response.putExtra(CERT_HANDLING_STRATEGY, certHandlingStrategy.getSelectedItemId());
                }

                setResult(RESULT_OK, response);
                break;
            case MODE_SPICE_CA_MANAGEMENT:
                storeImportedCaToFile();
                break;
            default:
                break;
        }

        finish();
    }

    @Click(R.id.btnLoad)
    void btnLoad() {
        downloadCa();
    }

    @OptionsItem(R.id.action_rest_ca_management)
    void restCaManagement() {
        mode = MODE_REST_CA_MANAGEMENT;
        updateViews();
        updateMenuItem();
    }

    @OptionsItem(R.id.action_spice_ca_management)
    void spiceCaManagement() {
        mode = MODE_SPICE_CA_MANAGEMENT;
        updateViews();
        updateMenuItem();
    }

    class CertHandlingSelectionChanged implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long index) {
            updateViews();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    @Background
    void downloadCa() {
        showProgressBar();
        String endpoint = txtCaUrl.getText().toString();
        URL url = null;
        try {
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

            caCert = caOutput.toByteArray();
            caInput = new ByteArrayInputStream(caOutput.toByteArray());

        } catch (IOException e) {
            hideProgressBar();
            showToast("Error loading certificate: " + e.getMessage());
            return;
        }

        try {
            Certificate ca = cf.generateCertificate(caInput);
            setCertDataToView(ca, url.getHost());
            this.certificate = ca;
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void setCertDataToView(Certificate ca, String urls) {
        if (txtCertDetails != null) {
            txtCertDetails.setText(ca != null ? ca.toString() : "");
        }

        if (txtValidForHostnames != null) {
            txtValidForHostnames.setText(urls != null ? urls : "");
        }
    }

    @Background
    public void deleteAllCaCerts() {
        providerFacade.deleteAll(OVirtContract.CaCert.CONTENT_URI);
    }

    @Background
    public void deleteCaFile() {
        File file = new File(Constants.getCaCertPath());
        if (file.isFile() && file.exists()) {
            file.delete();
        }
    }

    boolean storeImportedCaToFile() {
        if (caCert == null) {
            return false;
        }

        FileOutputStream out = null;
        try {
            File file = new File(Constants.getCaCertPath());
            if (file.getParent() != null) {
                File path = new File(file.getParent());
                path.mkdirs();
            }
            file.createNewFile();
            out = new FileOutputStream(file);
            out.write(caCert, 0, caCert.length);
        } catch (IOException e) {
            e.printStackTrace();
            showToast("Error storing certificate to file: " + e.getMessage());
            return false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return true;
    }

    boolean storeImportedCa() {
        if (certificate == null) {
            return false;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            try {
                out = new ObjectOutputStream(bos);
                out.writeObject(certificate);
                byte[] caAsBlob = bos.toByteArray();
                CaCert caCertEntity = new CaCert();
                // nvm, only support one
                caCertEntity.setId(1);
                caCertEntity.setContent(caAsBlob);
                caCertEntity.setValidFor(txtValidForHostnames.getText().toString());
                providerFacade.deleteAll(OVirtContract.CaCert.CONTENT_URI);
                providerFacade.batch().insert(caCertEntity).apply();
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
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }

        // if the output stream is not closed successfully the cert is still stored well
        return true;
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
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
}
