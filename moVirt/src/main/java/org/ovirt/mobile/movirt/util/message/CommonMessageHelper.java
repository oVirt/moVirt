package org.ovirt.mobile.movirt.util.message;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.rest.dto.ErrorBody;
import org.springframework.web.client.HttpClientErrorException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EBean
public class CommonMessageHelper {
    private static final String TAG = CommonMessageHelper.class.getSimpleName();

    private ObjectMapper mapper = new ObjectMapper();

    @RootContext
    Context context;

    public void showToast(String msg) {
        showToast(getAccount(), msg);
    }

    public void showShortToast(String msg) {
        showShortToast(getAccount(), msg);
    }

    public void showToast(MovirtAccount account, String msg) {
        showToast(account, msg, Toast.LENGTH_LONG);
    }

    public void showShortToast(MovirtAccount account, String msg) {
        showToast(account, msg, Toast.LENGTH_SHORT);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    protected void showToast(MovirtAccount account, String msg, int duration) {
        if (account != null) {
            msg = String.format("%s: %s", account.getName(), msg);
        }
        Toast.makeText(context, msg, duration).show();
    }

    public String createMessage(HttpClientErrorException ex) {
        String result = "";
        try {
            String responseBody = ex.getResponseBodyAsString();

            if (!TextUtils.isEmpty(responseBody)) {
                result = ex.getMessage() + ": " + responseBody;

                ErrorBody errorBody = mapper.readValue(ex.getResponseBodyAsByteArray(), ErrorBody.class);
                if (errorBody.fault != null) {
                    result = ex.getMessage() + " " + errorBody.fault.reason + " " + errorBody.fault.detail;
                } else {
                    ErrorBody.Fault fault = mapper.readValue(ex.getResponseBodyAsByteArray(), ErrorBody.Fault.class);
                    if (fault != null) {
                        result = ex.getMessage() + " " + fault.reason + " " + fault.detail;
                    }
                }
            }
        } catch (Exception f) {
            result = f.getMessage();
        }

        if (TextUtils.isEmpty(result)) {
            result = ex.getMessage();
        }

        return result;
    }

    /**
     * @see Message for default values to understand behaviour of overloaded methods
     * broadcasts error and logs it
     */
    @Background
    public void showError(MovirtAccount account, Message message) {
        ErrorType errorType = message.getType();
        Integer logPriority = message.getLogPriority();

        if (logPriority == null) {
            logPriority = errorType.getDefaultLogPriority();
        }

        showError(account, logPriority, messageToString(message));
    }

    public String messageToString(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("null message");
        }

        String header = message.getHeader();
        String detail = message.getDetail();

        String finalMessage;

        switch (message.getType()) {
            case REST:
                finalMessage = context.getString(R.string.rest_request_failed, getConnectionDetails() + detail);
                break;
            default:
                finalMessage = header == null ? detail :
                        context.getString(R.string.message_detailed_info, header, detail);
                break;
        }

        finalMessage = hidePasswords(finalMessage);
        return finalMessage;
    }

    private void showError(MovirtAccount account, int logPriority, String msg) {

        Log.println(logPriority, TAG, msg);

        Intent intent = new Intent(Broadcasts.ERROR_MESSAGE);
        intent.putExtra(Broadcasts.Extras.ERROR_HEADER, account == null ? null : account.getName());
        intent.putExtra(Broadcasts.Extras.ERROR_REASON, msg);
        context.sendBroadcast(intent);
    }

    protected MovirtAccount getAccount() {
        return null;
    }

    @NonNull
    protected String getConnectionDetails() {
        return "";
    }

    private String hidePasswords(String msg) {
        final Matcher matcher = Pattern.compile(".*password=([^&\"\\s']+).*", Pattern.DOTALL).matcher(msg);
        if (matcher.find()) {
            msg = msg.replaceAll("password=([^&\"\\s']+)", "password=" + String.format("%0" + matcher.group(1).length() + "d", 0).replace("0", "*"));
        }
        return msg;
    }

    public void showError(Message message) {
        showError(getAccount(), message);
    }

    public void showError(String message) {
        showError(getAccount(), new Message().setDetail(message));
    }

    public void showError(MovirtAccount account, String message) {
        showError(account, new Message().setDetail(message));
    }

    public void showError(Throwable message) {
        showError(getAccount(), new Message().setDetail(message));
    }

    public void showError(MovirtAccount account, Throwable message) {
        showError(account, new Message().setDetail(message));
    }

    public void showError(ErrorType type, Throwable message) {
        showError(getAccount(), new Message().setType(type).setDetail(message));
    }

    public void showError(MovirtAccount account, ErrorType type, Throwable message) {
        showError(account, new Message().setType(type).setDetail(message));
    }

    public void showError(ErrorType type, String message) {
        showError(getAccount(), new Message().setType(type).setDetail(message));
    }

    public void showError(MovirtAccount account, ErrorType type, String message) {
        showError(account, new Message().setType(type).setDetail(message));
    }

    public void showError(ErrorType type, String message, String header) {
        showError(getAccount(), new Message().setType(type).setDetail(message).setHeader(header));
    }

    public void showError(MovirtAccount account, ErrorType type, String message, String header) {
        showError(account, new Message().setType(type).setDetail(message).setHeader(header));
    }

    public void showError(ErrorType type, Throwable message, String header) {
        showError(getAccount(), new Message().setType(type).setDetail(message).setHeader(header));
    }

    public void showError(MovirtAccount account, ErrorType type, Throwable message, String header) {
        showError(account, new Message().setType(type).setDetail(message).setHeader(header));
    }
}
