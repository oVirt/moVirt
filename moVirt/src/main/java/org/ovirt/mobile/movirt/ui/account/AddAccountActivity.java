/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ovirt.mobile.movirt.ui.account;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.widget.EditText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.ui.BroadcastAwareAppCompatActivity;
import org.ovirt.mobile.movirt.ui.SettingsActivity_;
import org.ovirt.mobile.movirt.ui.auth.connectionsettings.ConnectionSettingsActivity_;
import org.ovirt.mobile.movirt.util.message.CommonMessageHelper;
import org.ovirt.mobile.movirt.util.message.ErrorType;
import org.ovirt.mobile.movirt.util.preferences.CommonSharedPreferencesHelper;

/**
 * Copied from AccountAuthenticatorActivity
 * <p>
 * Base class for implementing an Activity that is used to help implement an
 * AbstractAccountAuthenticator. If the AbstractAccountAuthenticator needs to use an activity
 * to handle the request then it can have the activity extend AccountAuthenticatorActivity.
 * The AbstractAccountAuthenticator passes in the response to the intent using the following:
 * <pre>
 *      intent.putExtra({@link AccountManager#KEY_ACCOUNT_AUTHENTICATOR_RESPONSE}, response);
 * </pre>
 * The activity then sets the result that is to be handed to the response via
 * {@link #setAccountAuthenticatorResult(android.os.Bundle)}.
 * This result will be sent as the result of the request when the activity finishes. If this
 * is never set or if it is set to null then error {@link AccountManager#ERROR_CODE_CANCELED}
 * will be called on the response.
 */
@EActivity(R.layout.activity_add_account)
public class AddAccountActivity extends BroadcastAwareAppCompatActivity {

    private AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;
    private Bundle mResultBundle = null;

    @ViewById(R.id.name)
    EditText name;

    @ViewById
    FloatingActionButton fab;

    @Bean
    AccountRxStore accountRxStore;

    @Bean
    CommonMessageHelper commonMessageHelper;

    @Bean
    CommonSharedPreferencesHelper commonSharedPreferencesHelper;

    @AfterViews
    void init() {
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.material_green_300)));
    }

    @Click(R.id.fab)
    void createAccount() {
        try {
            MovirtAccount account = accountRxStore.addAccount(name.getText().toString());
            finishWithResult(account);
        } catch (IllegalArgumentException e) {
            commonMessageHelper.showError(ErrorType.USER, e.getMessage());
        } catch (Exception e) {
            commonMessageHelper.showError(e);
        }
    }

    @OptionsItem(android.R.id.home)
    public void homeSelected() {
        onBackPressed();
    }

    private void finishWithResult(MovirtAccount account) {

        // deliver result as AccountAuthenticatorActivity
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.getName());
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, account.getType());
        setResult(RESULT_OK, intent);
        finish();

        // navigate into the connection settings
        Intent i = new Intent(this, SettingsActivity_.class);
        i.putExtra(Constants.ACCOUNT_KEY, account);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        startActivity(i);

        Intent j = new Intent(this, ConnectionSettingsActivity_.class);
        j.putExtra(Constants.ACCOUNT_KEY, account);
        startActivity(j);

        if (!commonSharedPreferencesHelper.isFirstAccountConfigured()) {
            commonMessageHelper.showToast(getString(R.string.first_connection_setup));
            commonSharedPreferencesHelper.setFirstAccountConfigured(true);
        }
    }

    /**
     * Set the result that is to be sent as the result of the request that caused this
     * Activity to be launched. If result is null or this method is never called then
     * the request will be canceled.
     *
     * @param result this is returned as the result of the AbstractAccountAuthenticator request
     */
    public final void setAccountAuthenticatorResult(Bundle result) {
        mResultBundle = result;
    }

    /**
     * Retreives the AccountAuthenticatorResponse from either the intent of the icicle, if the
     * icicle is non-zero.
     *
     * @param icicle the save instance data of this Activity, may be null
     */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mAccountAuthenticatorResponse =
                getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        if (mAccountAuthenticatorResponse != null) {
            mAccountAuthenticatorResponse.onRequestContinued();
        }
    }

    /**
     * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
     */
    @Override
    public void finish() {
        if (mAccountAuthenticatorResponse != null) {
            // send the result bundle back if set, otherwise send an error.
            if (mResultBundle != null) {
                mAccountAuthenticatorResponse.onResult(mResultBundle);
            } else {
                mAccountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED,
                        "canceled");
            }
            mAccountAuthenticatorResponse = null;
        }
        super.finish();
    }
}
