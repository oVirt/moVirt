package org.ovirt.mobile.movirt.rest;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.AccountEnvironment;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.ConnectionInfo;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.mainactivity.MainActivity_;
import org.ovirt.mobile.movirt.util.NotificationHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;
import org.ovirt.mobile.movirt.util.message.CommonMessageHelper;
import org.ovirt.mobile.movirt.util.message.ErrorType;
import org.ovirt.mobile.movirt.util.message.Message;
import org.ovirt.mobile.movirt.util.message.MessageHelper;
import org.ovirt.mobile.movirt.util.preferences.SharedPreferencesHelper;
import org.springframework.web.client.HttpClientErrorException;

@EBean
public class RestErrorHandler implements RequestHandler.ErrorHandler {
    private static final String TAG = RestErrorHandler.class.getSimpleName();

    private MovirtAccount account;

    private MessageHelper messageHelper;

    private SharedPreferencesHelper sharedPreferencesHelper;

    @RootContext
    Context context;

    @Bean
    ProviderFacade provider;

    @Bean
    NotificationHelper notificationHelper;

    @Bean
    CommonMessageHelper commonMessageHelper;

    public org.ovirt.mobile.movirt.rest.RestErrorHandler init(MovirtAccount account, MessageHelper messageHelper, SharedPreferencesHelper sharedPreferencesHelper) {
        ObjectUtils.requireAllNotNull(account, messageHelper, sharedPreferencesHelper);

        this.account = account;
        this.messageHelper = messageHelper;
        this.sharedPreferencesHelper = sharedPreferencesHelper;
        return this;
    }

    @Override
    public void resetErrors() {
        updateConnectionInfo(true, null);
    }

    @Override
    public void handleError(Throwable t) {
        handleError(t, null);
    }

    /**
     * @param throwable throwable
     * @param prefix    used only for NO_NETWORK_AVAILABLE
     */
    public void handleError(Throwable throwable, String prefix) {
        Message m = new Message().setType(ErrorType.REST);

        if (throwable instanceof RestCallException) {
            RestCallException e = (RestCallException) throwable;
            switch (e.getCallResult()) {
                case NO_TOKEN_AVAILABLE:
                    messageHelper.showError(m.setDetail(context.getString(R.string.rest_error_token_missing)));
                    break;
                case AUTH_FAILED:
                    handleConnectionError(m.setDetail(context.getString(R.string.rest_error_auth_failed)));
                    break;
                case NO_CONNECTION_SPECIFIED:
                    messageHelper.showError(m.setDetail(context.getString(R.string.rest_error_no_connection_specified)));
                    break;
                case WRONG_API_URL:
                    messageHelper.showError(m.setDetail(
                            context.getString(R.string.rest_url_missing, e.getCause().getMessage(), e.getMessage())));
                    break;
                case NO_NETWORK_AVAILABLE:
                    commonMessageHelper.showToast(((prefix == null) ? "" : prefix) + context.getString(R.string.rest_no_network));
                    break;
                case NO_CONNECTION:
                    handleConnectionError(m.setDetail(e.getCause()));
                    break;
                case HTTP_OTHER_ERROR:
                    if (e.getCause() instanceof HttpClientErrorException) {
                        messageHelper.showError(m.setDetail(messageHelper.createMessage((HttpClientErrorException) e.getCause())));
                        break; // intentional
                    }
                case OTHER_ERROR:
                default:
                    messageHelper.showError(m.setDetail(e.getCause()));
                    break;
            }
        } else {
            messageHelper.showError(m.setDetail(throwable));
        }
    }

    private void handleConnectionError(Message message) {
        String msg = messageHelper.messageToString(message);

        ConnectionInfo connectionInfo = updateConnectionInfo(false, msg);

        if (connectionInfo.getState() == ConnectionInfo.State.FAILED) { // first fail
            // show error
            messageHelper.showError(account, message);

            // show Notification
            if (sharedPreferencesHelper.isConnectionNotificationEnabled()) {
                Intent resultIntent = new Intent(context, MainActivity_.class);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                context,
                                0,
                                resultIntent,
                                0
                        );
                notificationHelper.showConnectionNotification(context, resultPendingIntent, connectionInfo);
            }
        } else {
            Log.e(TAG, msg);
        }
    }

    private ConnectionInfo updateConnectionInfo(boolean success, String description) {
        ConnectionInfo.State state;
        boolean prevFailed = false;

        ConnectionInfo connectionInfo = provider.query(ConnectionInfo.class)
                .where(OVirtContract.ConnectionInfo.ACCOUNT_ID, account.getId())
                .first();

        if (connectionInfo != null) {
            ConnectionInfo.State lastState = connectionInfo.getState();
            if (lastState == ConnectionInfo.State.FAILED || lastState == ConnectionInfo.State.FAILED_REPEATEDLY) {
                prevFailed = true;
            }
        } else {
            connectionInfo = new ConnectionInfo();
        }

        if (!success) {
            state = prevFailed ? ConnectionInfo.State.FAILED_REPEATEDLY : ConnectionInfo.State.FAILED;
        } else {
            state = ConnectionInfo.State.OK;
            description = null;
        }

        connectionInfo.setDescription(description);
        connectionInfo.updateWithCurrentTime(state);

        //update in DB
        if (connectionInfo.getId() == null) {
            connectionInfo.setIds(account.getId());
            provider.batch().insert(connectionInfo).apply();
        } else {
            provider.batch().update(connectionInfo).apply();
        }

        return connectionInfo;
    }
}

