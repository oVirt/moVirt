package org.ovirt.mobile.movirt.ui.auth.certificatemanagement;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.properties.PropertyUtils;
import org.ovirt.mobile.movirt.auth.properties.property.Cert;
import org.ovirt.mobile.movirt.auth.properties.property.CertHandlingStrategy;
import org.ovirt.mobile.movirt.auth.properties.property.CertLocation;
import org.ovirt.mobile.movirt.ui.BroadcastAwareAppCompatActivity;
import org.ovirt.mobile.movirt.ui.PresenterBroadcastAwareActivity;
import org.ovirt.mobile.movirt.ui.auth.certificatemanagement.data.CertTreeHolder;
import org.ovirt.mobile.movirt.ui.auth.certificatemanagement.data.CertTreeItem;
import org.ovirt.mobile.movirt.ui.auth.certificatemanagement.dialog.CertificateLocationChangeDialog;
import org.ovirt.mobile.movirt.ui.auth.certificatemanagement.dialog.DownloadCustomCertDialogFragment;
import org.ovirt.mobile.movirt.ui.auth.certificatemanagement.dialog.DownloadCustomCertDialogFragment_;
import org.ovirt.mobile.movirt.ui.auth.certificatemanagement.dialog.ImportFromFileCustomCertDialogFragment;
import org.ovirt.mobile.movirt.ui.auth.certificatemanagement.dialog.ImportFromFileCustomCertDialogFragment_;
import org.ovirt.mobile.movirt.ui.dialogs.ConfirmDialogFragment;
import org.ovirt.mobile.movirt.ui.mvp.BasePresenter;
import org.ovirt.mobile.movirt.util.message.CommonMessageHelper;

import java.net.URL;

import static org.ovirt.mobile.movirt.Constants.APP_PACKAGE_DOT;

@EActivity(R.layout.activity_certificate_management)
public class CertificateManagementActivity extends PresenterBroadcastAwareActivity
        implements CertificateLocationChangeDialog.ChangeLocationTypeListener,
        ConfirmDialogFragment.ConfirmDialogListener,
        ImportFromFileCustomCertDialogFragment.ImportIssuerListener,
        DownloadCustomCertDialogFragment.UrlListener,
        CertificateManagementContract.View {

    private static final String DOWNLOAD_CA_ISSUER = "downloadCaIssuer";
    private static final String IMPORT_CA_ISSUER = "importCaIssuer";
    private static final String DELETE_DIALOG_TAG = "deleteCert";
    private static final String DELETE_DIALOG_FROM_CUSTOM_LOCATION_LISTENER_TAG = "deleteCertFromCustomLocationListener";

    private static final int NORMAL_DELETE_DIALOG = 0;

    private static final int PICK_FILE_REQUEST_CODE = 0;

    private static final int MAX_VISIBLE_CERTIFICATES = 10;

    public final static String LOAD_CA_FROM = APP_PACKAGE_DOT + "ui.LOAD_CA_FROM";

    @ViewById
    Spinner certHandlingStrategySpinner;

    @ViewById
    RadioGroup certificateLocationRadioGroup;

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
    Button btnDownloadDefault;

    @ViewById
    Button btnDownloadCustom;

    @ViewById
    Button btnImportFromFile;

    @ViewById
    ProgressBar progress;

    @ViewById
    LinearLayout certTreeContainer;

    @ViewById
    LinearLayout customCertLayout;

    @ViewById
    LinearLayout buttonsLayout;

    @Bean
    CommonMessageHelper commonMessageHelper;

    private CertificateManagementPresenter presenter;

    @AfterViews
    void init() {
        presenter = CertificateManagementPresenter_.getInstance_(getApplicationContext())
                .setView(this)
                .setAccount(getIntent().getParcelableExtra(Constants.ACCOUNT_KEY))
                .setApiUrl(getIntent().getStringExtra(LOAD_CA_FROM))
                .setMaxVisibleCertificates(MAX_VISIBLE_CERTIFICATES)
                .initialize();

        certHandlingStrategySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                presenter.onCertHandlingStrategyChanged(CertHandlingStrategy.from(id));
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
                presenter.onValidHostnamesChanged(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected BasePresenter getPresenter() {
        return presenter;
    }

    @CheckedChange(R.id.radio_button_default_url)
    void defaultUrlChecked(CompoundButton button, boolean isChecked) {
        if (isChecked) {
            presenter.onCertificateLocationChangeAttempt(CertLocation.DEFAULT_URL);
        }
    }

    @CheckedChange(R.id.radio_button_custom_url)
    void customUrlChecked(CompoundButton button, boolean isChecked) {
        if (isChecked) {
            presenter.onCertificateLocationChangeAttempt(CertLocation.CUSTOM_URL);
        }
    }

    @CheckedChange(R.id.radio_button_import_from_file)
    void importFromFileChecked(CompoundButton button, boolean isChecked) {
        if (isChecked) {
            presenter.onCertificateLocationChangeAttempt(CertLocation.FILE);
        }
    }

    @Click(R.id.btnDownloadDefault)
    void btnDownLoadDefault() {
        presenter.downloadDefaultCertificate();
    }

    @Click(R.id.btnDownloadCustom)
    void btnDownloadCustom() {
        presenter.downloadCustomCertificate();
    }

    @Override
    public void onImportIssuer() {
        btnImportFromFile();
    }

    @Click(R.id.btnImportFromFile)
    void btnImportFromFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;
        if (getPackageManager().resolveActivity(sIntent, 0) != null) {
            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Open file");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent});
        } else {
            chooserIntent = Intent.createChooser(intent, "Open file");
        }

        try {
            startActivityForResult(chooserIntent, PICK_FILE_REQUEST_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            commonMessageHelper.showToast("No suitable File Manager was found.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FILE_REQUEST_CODE) {
            presenter.importCustomCertificateFromFile(data == null ? null : data.getData());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onNewDialogUrl(URL url, boolean startNewChain) {
        presenter.downloadCertificate(new URL[]{url}, startNewChain);
    }

    @Click(R.id.btnDelete)
    void btnDelete() {
        DialogFragment confirmDialog = ConfirmDialogFragment
                .newInstance(NORMAL_DELETE_DIALOG, getString(R.string.dialog_action_delete_certificate));
        confirmDialog.show(getFragmentManager(), DELETE_DIALOG_TAG);
    }

    @Override
    public void onLocationChange(CertLocation certLocation) {
        presenter.toggleCustomCertificateLocation(certLocation);
    }

    @Override
    public void onDialogResult(int dialogButton, int actionId) {
        if (dialogButton == DialogInterface.BUTTON_POSITIVE && actionId == NORMAL_DELETE_DIALOG) {
            presenter.deleteAllCertsInBackground();
        }
    }

    @Override
    public void displayTitle(String title) {
        setTitle(getString(R.string.account_certificate_management, title));
    }

    @Override
    public void showChangeCertificateLocationDeleteDialog(CertLocation certLocation) {
        if (getFragmentManager().findFragmentByTag(DELETE_DIALOG_FROM_CUSTOM_LOCATION_LISTENER_TAG) == null) {
            // delete certs before switching mode
            DialogFragment confirmDialog = CertificateLocationChangeDialog
                    .newInstance(certLocation, getString(R.string.dialog_action_delete_certificate),
                            getString(R.string.dialog_action_delete_certificate_header));

            getFragmentManager().beginTransaction()
                    .add(confirmDialog, DELETE_DIALOG_FROM_CUSTOM_LOCATION_LISTENER_TAG)
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void showImportFromFileCustomCaDialog() {
        if (getFragmentManager().findFragmentByTag(IMPORT_CA_ISSUER) == null) {
            ImportFromFileCustomCertDialogFragment dialog = new ImportFromFileCustomCertDialogFragment_();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(dialog, IMPORT_CA_ISSUER);
            transaction.commitAllowingStateLoss();
        }
    }

    @Override
    public void showDownloadCustomCaDialog(String url, boolean startNewChain) {
        if (getFragmentManager().findFragmentByTag(DOWNLOAD_CA_ISSUER) == null) { // updateViews can call this multiple times
            DownloadCustomCertDialogFragment dialog = new DownloadCustomCertDialogFragment_();
            dialog.setUrl(url);
            dialog.setStartNewChain(startNewChain);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(dialog, DOWNLOAD_CA_ISSUER);
            transaction.commitAllowingStateLoss();
        }
    }

    @Override
    public void selectCertHandlingStrategy(CertHandlingStrategy certHandlingStrategy) {
        certHandlingStrategySpinner.setSelection((int) certHandlingStrategy.id());

        boolean visible = certHandlingStrategy == CertHandlingStrategy.TRUST_CUSTOM;
        buttonsLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
        customCertLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void selectCertLocationType(CertLocation certLocation) {
        ((RadioButton) certificateLocationRadioGroup.getChildAt(certLocation.id())).setChecked(true);
    }

    @Override
    public void showDownloadButton(CertLocation certLocation, boolean showDownloadButtons, boolean hasCerts) {
        btnDownloadDefault.setVisibility(showDownloadButtons && certLocation == CertLocation.DEFAULT_URL ? View.VISIBLE : View.GONE);

        btnDownloadCustom.setVisibility(showDownloadButtons && certLocation == CertLocation.CUSTOM_URL ? View.VISIBLE : View.GONE);
        btnDownloadCustom.setText(hasCerts ? R.string.download_issuer : R.string.download);

        btnImportFromFile.setVisibility(showDownloadButtons && certLocation == CertLocation.FILE ? View.VISIBLE : View.GONE);
        btnImportFromFile.setText(hasCerts ? R.string.import_issuer_from_file : R.string.import_from_file);

        btnDelete.setVisibility(hasCerts ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showHostnames(String hostnames) {
        // if changed from the UI
        if (hostnames != null && !hostnames.equals(PropertyUtils.catenateToCsv(PropertyUtils.parseHostnames(txtValidForHostnames.getText().toString())))) {
            txtValidForHostnames.setText(hostnames);
        }
    }

    @Override
    public void showCustomCertInnerDetailVisibility(boolean visible) {
        btnDelete.setEnabled(visible);
        txtValidForHostnames.setVisibility(visible ? View.VISIBLE : View.GONE);
        validHostnamesLabel.setVisibility(visible ? View.VISIBLE : View.GONE);

        txtCertUrl.setVisibility(visible ? View.VISIBLE : View.GONE);
        certUrlLabel.setVisibility(visible ? View.VISIBLE : View.GONE);

        txtCertDetails.setVisibility(visible ? View.VISIBLE : View.GONE);
        txtCertDetails.setText(null);

        certTreeContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showCerts(Cert[] certs) {
        LinearLayout container = certTreeContainer;
        if (certs == null || certs.length == 0) {
            certTreeContainer.removeAllViews(); // remove previous tree
            return;
        }
        TreeNode root = TreeNode.root();
        TreeNode intermediateLeaf = root;
        CertTreeHolder leafHolder = null;
        int visibleCertificates;

        if (certs.length > MAX_VISIBLE_CERTIFICATES) {
            visibleCertificates = MAX_VISIBLE_CERTIFICATES;
        } else {
            visibleCertificates = certs.length;
        }

        CertTreeItem.CertificateSelectedListener listener = (certificate, location) -> {
            txtCertDetails.setText(certificate.toString());
            txtCertUrl.setText(location);
        };

        for (int i = visibleCertificates - 1; i >= 0; i--) { // create tree hierarchy
            CertTreeItem data = new CertTreeItem(certs[i], listener);
            leafHolder = new CertTreeHolder(this);
            TreeNode newNode = new TreeNode(data).setViewHolder(leafHolder);

            intermediateLeaf.addChild(newNode);
            intermediateLeaf.setExpanded(true);
            intermediateLeaf = newNode;
        }

        AndroidTreeView atv = new AndroidTreeView(this, root);
        atv.setUseAutoToggle(false);
        atv.setSelectionModeEnabled(true);
        atv.setDefaultContainerStyle(R.style.SmallPaddingTreeNode);
        container.addView(atv.getView());
        if (leafHolder != null) {
            leafHolder.selectNode(); // select Api certificate
        }
    }

    @Override
    public void showCertificateDownloadInProgress(boolean inProgress) {
        progress.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        certHandlingStrategySpinner.setEnabled(!inProgress);
        txtValidForHostnames.setEnabled(!inProgress);
        btnDelete.setEnabled(!inProgress);
        btnDownloadDefault.setEnabled(!inProgress);
        btnDownloadCustom.setEnabled(!inProgress);
        btnImportFromFile.setEnabled(!inProgress);
        for (int i = 0; i < certificateLocationRadioGroup.getChildCount(); i++) {
            certificateLocationRadioGroup.getChildAt(i).setEnabled(!inProgress);
        }
        certificateLocationRadioGroup.setEnabled(!inProgress);
    }

    @OptionsItem(android.R.id.home)
    public void homeSelected() {
        finish();
    }
}
