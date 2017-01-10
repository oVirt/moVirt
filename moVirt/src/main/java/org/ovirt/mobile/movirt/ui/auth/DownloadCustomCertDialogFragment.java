package org.ovirt.mobile.movirt.ui.auth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
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
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.ui.UiUtils;
import org.ovirt.mobile.movirt.ui.dialogs.DialogListener;
import org.ovirt.mobile.movirt.util.URIUtils;
import org.ovirt.mobile.movirt.util.message.MessageHelper;

import java.net.URL;

@EFragment
public class DownloadCustomCertDialogFragment extends DialogFragment {

    private DialogListener.UrlListener listenerActivity;

    private MultiAutoCompleteTextView urlText;

    @InstanceState
    String url;

    @InstanceState
    boolean startNewChain;

    @Bean
    MessageHelper messageHelper;

    @Bean
    AccountPropertiesManager propertiesManager;

    Context context;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listenerActivity = (DialogListener.UrlListener) activity;
            context = activity.getApplicationContext();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement UrlListener");
        }
    }

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
        ArrayAdapter<String> urlAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, new String[]{"http://"});
        urlText.setAdapter(urlAdapter);
        urlText.setTokenizer(UiUtils.getUrlTokenizer());
        urlText.setText(url);

        urlText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    urlText.clearFocus();
                }
                return false;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, leave this method for button instantiation (Older Android Versions). Then use it in  onStart
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

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
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        URL url = URIUtils.tryToParseUrl(urlText.getText().toString());
                        listenerActivity.onNewDialogUrl(url, startNewChain);
                        dialog.dismiss();
                    } catch (IllegalArgumentException parseError) {
                        messageHelper.showToast(parseError.getMessage());
                    }
                }
            });
        }
    }
}
