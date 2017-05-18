package org.ovirt.mobile.movirt.ui.auth.connectionsettings;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.ui.BroadcastAwareAppCompatActivity;
import org.ovirt.mobile.movirt.ui.PresenterBroadcastAwareActivity;
import org.ovirt.mobile.movirt.ui.UiUtils;
import org.ovirt.mobile.movirt.ui.auth.certificatemanagement.CertificateManagementActivity;
import org.ovirt.mobile.movirt.ui.auth.certificatemanagement.CertificateManagementActivity_;
import org.ovirt.mobile.movirt.ui.auth.connectionsettings.dialog.FixApiPathDialogFragment;
import org.ovirt.mobile.movirt.ui.auth.connectionsettings.exception.SmallMistakeException;
import org.ovirt.mobile.movirt.ui.auth.connectionsettings.exception.WrongApiPathException;
import org.ovirt.mobile.movirt.ui.auth.connectionsettings.exception.WrongArgumentException;
import org.ovirt.mobile.movirt.util.message.CommonMessageHelper;
import org.ovirt.mobile.movirt.util.message.ErrorType;

@EActivity(R.layout.activity_connection_settings)
public class ConnectionSettingsActivity extends PresenterBroadcastAwareActivity implements ConnectionSettingsContract.View {

    private static final String TAG = ConnectionSettingsActivity.class.getSimpleName();
    private static final String[] URL_COMPLETE = {"http://", "https://", "ovirt-engine/api", "80",
            "443", "api"};
    private static final String[] USERNAME_COMPLETE = {"admin@", "internal", "admin@internal"};

    @ViewById
    MultiAutoCompleteTextView txtEndpoint;

    @ViewById
    MultiAutoCompleteTextView txtUsername;

    @ViewById
    EditText txtPassword;

    @ViewById
    ImageView passwordVisibility;

    @ViewById
    ProgressBar authProgress;

    @ViewById
    FloatingActionButton btnCreate;

    @ViewById
    Button btnAdvanced;

    @Bean
    CommonMessageHelper commonMessageHelper;

    private ConnectionSettingsContract.Presenter presenter;

    @AfterViews
    void init() {
        initView();
        presenter = ConnectionSettingsPresenter_.getInstance_(getApplicationContext())
                .setView(this)
                .setAccount(getIntent().getParcelableExtra(Constants.ACCOUNT_KEY))
                .initialize();
    }

    private void initView() {
        btnCreate.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.material_green_300)));

        ArrayAdapter<String> urlAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, URL_COMPLETE);
        txtEndpoint.setAdapter(urlAdapter);
        txtEndpoint.setTokenizer(UiUtils.getUrlTokenizer());

        ArrayAdapter<String> usernameAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, USERNAME_COMPLETE);
        txtUsername.setAdapter(usernameAdapter);
        txtUsername.setTokenizer(UiUtils.getUsernameTokenizer());

        passwordVisibility.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageView imgView = (ImageView) view;
                    imgView.setBackgroundColor(UiUtils.addAlphaToColor(Color.WHITE, 0.25f));
                    imgView.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    ImageView imgView = (ImageView) view;
                    imgView.setBackground(null);
                    imgView.invalidate();
                    break;
                }
            }

            return false;
        });
    }

    public ConnectionSettingsContract.Presenter getPresenter() {
        return presenter;
    }

    @Click(R.id.passwordVisibility)
    void togglePasswordVisibility() {
        presenter.togglePasswordVisibility();
    }

    @Click(R.id.btnCreate)
    void login() {
        try {
            LoginInfo loginInfo = new LoginInfo(
                    txtEndpoint.getText().toString(),
                    txtUsername.getText().toString(),
                    txtPassword.getText().toString(),
                    true); // admin only now
            presenter.sanitizeAndCheckLoginInfo(loginInfo);
            presenter.login(loginInfo); // background call
        } catch (SmallMistakeException e) {
            commonMessageHelper.showToast(e.getMessage());
        } catch (WrongArgumentException e) {
            commonMessageHelper.showError(e.getAccount(), ErrorType.USER, e.getMessage());
        } catch (WrongApiPathException e) {
            DialogFragment apiPathDialog = FixApiPathDialogFragment.newInstance(e.getAccount().getName(), e.getLoginInfo());
            apiPathDialog.show(getFragmentManager(), "apiPathDialog"); // fires login
        }
    }

    @Override
    public void setPasswordVisibility(boolean visibility) {
        passwordVisibility.setImageResource(visibility ? R.drawable.ic_visibility_white_24dp :
                R.drawable.ic_visibility_off_white_24dp);
        txtPassword.setInputType(visibility ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    @Override
    public void displayUserName(String userName) {
        txtUsername.setText(userName);
    }

    @Override
    public void displayPassword(String password) {
        txtPassword.setText(password);
    }

    @Override
    public void displayApiUrl(String apiUrl) {
        txtEndpoint.setText(apiUrl);
    }

    @Override
    public void displayTitle(String title) {
        setTitle(getString(R.string.title_activity_account_connection_settings, title));
    }

    @Click(R.id.btnAdvanced)
    public void btnAdvancedClicked() {
        presenter.onCertificateManagementClicked(txtEndpoint.getText().toString());
    }

    @Override
    public void showLoginInProgress(boolean loginInProgress) {
        boolean enabled = !loginInProgress;
        txtEndpoint.setEnabled(enabled);
        txtUsername.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
        passwordVisibility.setEnabled(enabled);
        btnAdvanced.setEnabled(enabled);
        btnCreate.setEnabled(enabled);
        btnCreate.setVisibility(loginInProgress ? View.GONE : View.VISIBLE);
        authProgress.setVisibility(loginInProgress ? View.VISIBLE : View.GONE);
    }

    @Override
    public void startCertificateManagementActivity(MovirtAccount account, String apiUrl) {
        Intent intent = new Intent(this, CertificateManagementActivity_.class);
        intent.putExtra(Constants.ACCOUNT_KEY, account);
        intent.putExtra(CertificateManagementActivity.LOAD_CA_FROM, apiUrl);
        startActivity(intent);
    }

    @OptionsItem(android.R.id.home)
    public void homeSelected() {
        onBackPressed();
    }
}
