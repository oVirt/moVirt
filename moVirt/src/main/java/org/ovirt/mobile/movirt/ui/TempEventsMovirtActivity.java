package org.ovirt.mobile.movirt.ui;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.ovirt.mobile.movirt.sync.EventsHandler;

/**
 * Class that represents base Activity for entire moVirt app. Every Activity should extends this if
 * you want to see basic ActionBar options, etc
 * Created by Nika on 25.06.2015.
 */

@EActivity
public abstract class TempEventsMovirtActivity extends MovirtActivity {

    @Bean
    protected EventsHandler eventsHandler;

    @Override
    protected void onPause() {
        if (isFinishing()) {
            discardTemporaryEvents();
        }
        super.onPause();
    }

    @Background
    protected void discardTemporaryEvents() {
        eventsHandler.discardTemporaryEvents();
    }
}
