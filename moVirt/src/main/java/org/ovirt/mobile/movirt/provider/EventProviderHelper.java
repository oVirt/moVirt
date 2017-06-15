package org.ovirt.mobile.movirt.provider;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.util.message.MessageHelper;
import org.ovirt.mobile.movirt.util.preferences.SharedPreferencesHelper;

@EBean
public class EventProviderHelper {

    private MovirtAccount account;
    private SharedPreferencesHelper sharedPreferencesHelper;
    private MessageHelper messageHelper;

    @Bean
    ProviderFacade provider;

    public EventProviderHelper init(MovirtAccount account, SharedPreferencesHelper sharedPreferencesHelper, MessageHelper messageHelper) {
        this.account = account;
        this.sharedPreferencesHelper = sharedPreferencesHelper;
        this.messageHelper = messageHelper;
        return this;
    }

    public int deleteEvents() {
        return provider.query(Event.class)
                .where(OVirtContract.Event.ACCOUNT_ID, account.getId())
                .delete();
    }

    public void deleteTemporaryEvents() {
        provider.query(Event.class)
                .where(OVirtContract.Event.ACCOUNT_ID, account.getId())
                .where(OVirtContract.Event.TEMPORARY, "0", Relation.LARGER_THAN)
                .delete();
    }

    public void deleteOldEvents() {
        try {
            deleteEventsAndLetOnly(sharedPreferencesHelper.getMaxEventsStored());
        } catch (Exception e) {
            messageHelper.showError(e);
        }
    }

    private void deleteEventsAndLetOnly(int leave) {
        if (leave < 1) {
            deleteEvents();
            return;
        }

        Event event = provider.query(Event.class)
                .where(OVirtContract.Event.ACCOUNT_ID, account.getId())
                .orderByDescending(OVirtContract.Event.SHORT_ID)
                .limit(leave)
                .last();

        if (event != null) {
            provider.query(Event.class)
                    .where(OVirtContract.Event.ACCOUNT_ID, account.getId())
                    .where(OVirtContract.Event.SHORT_ID, event.getShortId(), Relation.LESS_THAN)
                    .delete();
        }
    }
}
