package org.ovirt.mobile.movirt.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.util.Log;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.AccountManagerHelper;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.account.AccountEnvironment;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.auth.account.EnvironmentStore;
import org.ovirt.mobile.movirt.auth.account.data.LoginStatus;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.auth.account.data.SyncStatus;
import org.ovirt.mobile.movirt.rest.RestCallException;
import org.ovirt.mobile.movirt.util.message.CommonMessageHelper;
import org.ovirt.mobile.movirt.util.preferences.SharedPreferencesHelper;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

@EBean(scope = EBean.Scope.Singleton)
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = SyncAdapter.class.getSimpleName();

    private static final int MAX_SYNC_ERRORS = 2; // for all facade syncs combined
    private static final int WAIT_BEFORE_NEXT_TRY = 2; // seconds

    @Bean
    SharedPreferencesHelper sharedPreferencesHelper;

    @Bean
    CommonMessageHelper commonMessageHelper;

    @Bean
    AccountRxStore rxStore;

    @Bean
    AccountManagerHelper accountManagerHelper;

    @Bean
    EnvironmentStore environmentStore;

    public SyncAdapter(Context context) {
        super(context, true, true);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient providerClient, SyncResult syncResult) {
        try {
            final MovirtAccount movirtAccount = accountManagerHelper.asMoAccount(account);
            final AccountEnvironment environment = environmentStore.getEnvironment(movirtAccount);

            if (environment.isLoginInProgress()) {
                // sync will be called again if the login succeeds
                return;
            }
            // onPerformSync calls are guaranteed to be serialized for the same account
            // so progress is atomic
            setSyncInProgress(movirtAccount, true);

            // remember last used sync action, because we may try it again if it fails
            final Subject<SyncAction> syncActions = BehaviorSubject.createDefault(SyncAction.getFirstAction());
            final Observable<LoginStatus> loginStatus = rxStore.isLoginInProgressObservable(movirtAccount)
                    .observeOn(Schedulers.newThread());

            Observable.combineLatest(syncActions, loginStatus, SyncBundle::new)
                    .doOnNext(syncBundle -> { // sync
                        if (syncBundle.action == SyncAction.EVENT
                                && !environment.getSharedPreferencesHelper().isPollEventsEnabled()) {
                            return;
                        }
                        environment.getFacade(syncBundle.action.getClazz()).syncAllUnsafe();
                    })
                    .retryWhen(errors ->
                            // run again with another MAX_SYNC_ERRORS tries; + 1 is for signaling the last error
                            errors.zipWith(Observable.range(1, MAX_SYNC_ERRORS + 1), Pair::new)
                                    .flatMap(err -> {
                                        if (err.second == MAX_SYNC_ERRORS + 1 // last try failed
                                                // or unrecoverable exception
                                                || ((err.first instanceof RestCallException) && !((RestCallException) err.first).isRepeatable())) {
                                            return Observable.<Long>error(err.first); // cancel the sync
                                        }
                                        // wait few seconds before trying again
                                        Log.d(TAG, String.format("Account %s: failed sync. Retrying...", movirtAccount.getName()));
                                        return Observable.timer(WAIT_BEFORE_NEXT_TRY, TimeUnit.SECONDS);
                                    }))
                    .doFinally(() -> setSyncInProgress(movirtAccount, false))
                    //  finish if there is no reason to continue syncing
                    .takeWhile(SyncBundle::isNotFinished)
                    // block onPerformSync method -> the sync status will be atomic
                    .blockingSubscribe(syncBundle -> syncActions.onNext(syncBundle.action.getNextAction()), // continue sync with next action
                            throwable -> {
                                // android can interrupt us while we sleep, probably because the same sync is pending; so ignore this one
                                if (!(throwable instanceof InterruptedException)) {
                                    // if not, first real error is handled (depends on MAX_SYNC_ERRORS)
                                    environment.getRestErrorHandler().handleError(throwable, "Sync failed. ");
                                }
                            });
        } catch (AccountDeletedException | IllegalStateException ignore) {
        }
    }

    private void setSyncInProgress(MovirtAccount account, boolean inSync) {
        rxStore.SYNC_STATUS.onNext(new SyncStatus(account, inSync));
    }

    private class SyncBundle {
        SyncAction action;
        boolean isLoginInProgress;

        SyncBundle(SyncAction action, LoginStatus loginStatus) {
            this.action = action;
            this.isLoginInProgress = loginStatus.isInProgress();
        }

        boolean isNotFinished() {
            // somebody canceled our sync or finish ahead of time if login is in progress
            return !Thread.interrupted() && !isLoginInProgress && action.hasNextAction();
        }
    }
}
