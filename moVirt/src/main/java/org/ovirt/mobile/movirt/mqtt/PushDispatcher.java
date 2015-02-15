package org.ovirt.mobile.movirt.mqtt;

import android.content.Context;
import android.util.Log;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.ovirt.mobile.movirt.facade.EntityFacade;
import org.ovirt.mobile.movirt.facade.HostFacade;
import org.ovirt.mobile.movirt.facade.VmFacade;
import org.ovirt.mobile.movirt.model.OVirtEntity;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.ProviderFacade;

import java.util.ArrayList;
import java.util.List;

@EBean
public class PushDispatcher {

    private static final String TAG = PushDispatcher.class.getSimpleName();

    private static final String CREATED_MESSAGE = "+";
    private static final String REMOVED_MESSAGE = "-";

    private static final String ALL = "/#";
    private static final String VMS_TOPIC = "vm";
    private static final String HOSTS_TOPIC = "host";
    private static final String EVENTS_TOPIC = "event";

    private final List<Updater> updaters = new ArrayList<>();

    @RootContext
    Context context;

    @Bean
    ProviderFacade provider;

    @Bean
    VmFacade vmFacade;

    @Bean
    HostFacade hostFacade;

    interface Updater {
        boolean matches(String topic);
        String getPrefix();
        void performUpdate(String id, String message);
    }

    static abstract class BaseUpdater implements Updater {

        protected final String prefix;

        BaseUpdater(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public boolean matches(String topic) {
            return topic.startsWith(prefix);
        }

        @Override
        public String getPrefix() {
            return prefix;
        }
    }

    class EntityUpdater<E extends OVirtEntity> extends BaseUpdater {

        private final EntityFacade<E> facade;

        EntityUpdater(String prefix, EntityFacade<E> facade) {
            super(prefix);
            this.facade = facade;
        }

        public void performUpdate(String id, String message) {
            switch (message) {
                case REMOVED_MESSAGE:
                    provider.deleteAll(Vm.CONTENT_URI.buildUpon().appendPath(id).build());
                    break;
                case CREATED_MESSAGE:
                default:
                    Log.i(TAG, "Performing update");
                    facade.sync(id, null);
                    break;
            }
        }
    }

    @AfterInject
    void initUpdaters() {
        updaters.add(new EntityUpdater<>(VMS_TOPIC, vmFacade));
        updaters.add(new EntityUpdater<>(HOSTS_TOPIC, hostFacade));
    }

    public void pushReceived(String topic, String message) {
        for (Updater updater : updaters) {
            if (updater.matches(topic)) {
                String id = topic.substring(updater.getPrefix().length() + 1);
                updater.performUpdate(id, message);
            }
        }
    }

    public List<String> getTopics() {
        List<String> topics = new ArrayList<>();
        for (Updater updater : updaters) {
            topics.add(updater.getPrefix() + ALL);
        }
        return topics;
    }
}
