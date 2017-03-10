package org.ovirt.mobile.movirt.ui.dialogs;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.ovirt.mobile.movirt.R;

public class WebMessageEditTextPreference extends EditTextPreference {

    public WebMessageEditTextPreference(Context context) {
        super(context);
        setDialogResource();
    }

    public WebMessageEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogResource();
    }

    public WebMessageEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setDialogResource();
    }

    private void setDialogResource() {
        setDialogLayoutResource(R.layout.web_message_preference_dialog_edittext_material);
    }

    @Override
    protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
        ViewGroup container = (ViewGroup) dialogView
                .findViewById(R.id.edittext_container);
        if (container != null) {
            container.addView(editText, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView message = (TextView) dialogView.findViewById(R.id.message);
        if (message != null && getKey() != null) {
            int resourceId = 0;
            if (getKey().equals(getContext().getString(R.string.vms_search_query_pref_key))) {
                resourceId = R.string.vms_search_query_help;
            } else if (getKey().equals(getContext().getString(R.string.events_search_query_pref_key))) {
                resourceId = R.string.events_search_query_help;
            }
            message.setText(getContext().getText(resourceId));
            message.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }


}
