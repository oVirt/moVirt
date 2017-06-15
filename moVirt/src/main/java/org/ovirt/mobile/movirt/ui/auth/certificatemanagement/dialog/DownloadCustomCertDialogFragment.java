package org.ovirt.mobile.movirt.ui.auth.certificatemanagement.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.UiUtils;
import org.ovirt.mobile.movirt.ui.dialogs.ListenerDialogFragment;
import org.ovirt.mobile.movirt.util.URIUtils;
import org.ovirt.mobile.movirt.util.message.CommonMessageHelper;

import java.net.URL;

@EFragment
public class DownloadCustomCertDialogFragment extends ListenerDialogFragment<DownloadCustomCertDialogFragment.UrlListener> {

    private MultiAutoCompleteTextView urlText;

    @InstanceState
    String url;

    @InstanceState
    boolean startNewChain;

    @Bean
    CommonMessageHelper commonMessageHelper;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setStartNewChain(boolean startNewChain) {
        this.startNewChain = startNewChain;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.download_custom_cert_dialog, null);
        final TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(getString(startNewChain ? R.string.cert_dialog_download_custom : R.string.cert_dialog_download_issuer));

        final TextView reason = (TextView) view.findViewById(R.id.issuerReason);
        reason.setVisibility(startNewChain ? View.GONE : View.VISIBLE);

        urlText = (MultiAutoCompleteTextView) view.findViewById(R.id.urlField);
        ArrayAdapter<String> urlAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, new String[]{"http://"});
        urlText.setAdapter(urlAdapter);
        urlText.setTokenizer(UiUtils.getUrlTokenizer());
        urlText.setText(url);

        urlText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                urlText.clearFocus();
            }
            return false;
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(R.string.download, (dialog, which) -> {
            // Do nothing, leave this method for button instantiation (Older Android Versions). Then use it in  onStart
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            final Button button = ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_POSITIVE);

            if (button != null) {
                button.setEnabled(!TextUtils.isEmpty(url));
                urlText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        button.setEnabled(s.length() != 0);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
            }
        });

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                try {
                    URL url1 = URIUtils.tryToParseUrl(urlText.getText().toString());
                    getListener().onNewDialogUrl(url1, startNewChain);
                    dialog.dismiss();
                } catch (IllegalArgumentException parseError) {
                    commonMessageHelper.showToast(parseError.getMessage());
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        urlText = null;
    }

    public interface UrlListener {
        void onNewDialogUrl(URL url, boolean startNewChain);
    }
}
