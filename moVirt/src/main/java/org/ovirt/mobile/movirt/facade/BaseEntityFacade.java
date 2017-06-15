package org.ovirt.mobile.movirt.facade;

import android.database.Cursor;
import android.util.Log;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.facade.intent.EntityIntentResolver;
import org.ovirt.mobile.movirt.facade.intent.IntentResolvers;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;
import org.ovirt.mobile.movirt.model.mapping.EntityMapper;
import org.ovirt.mobile.movirt.rest.CompositeResponse;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.RequestHandler;
import org.ovirt.mobile.movirt.rest.Response;
import org.ovirt.mobile.movirt.rest.RestCallException;
import org.ovirt.mobile.movirt.rest.client.OVirtClient;
import org.ovirt.mobile.movirt.sync.DbUpdater;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import java.util.List;

@EBean
public abstract class BaseEntityFacade<E extends OVirtEntity> implements EntityFacade<E> {
    public static final String TAG = BaseEntityFacade.class.getSimpleName();

    @Bean
    DbUpdater dbUpdater;

    @Bean
    IntentResolvers intentResolvers;

    protected MovirtAccount account;

    protected OVirtClient oVirtClient;

    protected RequestHandler requestHandler;

    protected AccountPropertiesManager propertiesManager;

    protected final Class<E> clazz;

    protected BaseEntityFacade(Class<E> clazz) {
        this.clazz = clazz;
    }

    public BaseEntityFacade<E> init(AccountPropertiesManager propertiesManager, OVirtClient oVirtClient, RequestHandler requestHandler) {
        ObjectUtils.requireAllNotNull(oVirtClient, requestHandler);

        this.propertiesManager = propertiesManager;
        this.account = propertiesManager.getManagedAccount();
        this.oVirtClient = oVirtClient;
        this.requestHandler = requestHandler;
        return this;
    }

    @Override
    public EntityIntentResolver<E> getIntentResolver() {
        return intentResolvers.getResolver(clazz);
    }

    protected Request<E> getSyncOneRestRequest(String id, String... ids) {
        throw new UnsupportedOperationException("Sync for one entity not implemented.");
    }

    protected abstract Request<List<E>> getSyncAllRestRequest(String... ids);

    protected CompositeResponse<E> getSyncOneResponse(Response<E> response, String... ids) {
        return respond().asUpdateEntityResponse().addResponse(response);
    }

    protected Response<List<E>> getSyncAllResponse(Response<List<E>> response, String... ids) {
        return respond().asUpdateEntitiesResponse().addResponse(response);
    }

    protected DbUpdater.UpdateLocalEntitiesBuilder<E> respond() {
        return dbUpdater.update(clazz).whereAccount(account.getId());
    }

    protected <T extends OVirtEntity> DbUpdater.UpdateLocalEntitiesBuilder<T> respond(Class<T> clazzz) {
        return dbUpdater.update(clazzz).whereAccount(account.getId());
    }

    @Override
    public E mapFromCursor(Cursor cursor) {
        return EntityMapper.forEntity(clazz).fromCursor(cursor);
    }

    @Override
    public List<E> mapAllFromCursor(Cursor cursor) {
        return EntityMapper.forEntity(clazz).listFromCursor(cursor);
    }

    @Override
    public void syncOne(Response<E> response, String id, String... ids) {
        log("one", ids.length + 1);
        requestHandler.fireRestRequestSafe(getSyncOneRestRequest(id, ids), getSyncOneResponse(response, ids));
    }

    @Override
    public void syncAll(String... ids) {
        syncAll(null, ids);
    }

    @Override
    public void syncAll(Response<List<E>> response, String... ids) {
        log("all", ids.length);
        requestHandler.fireRestRequestSafe(getSyncAllRestRequest(ids), getSyncAllResponse(response, ids));
    }

    @Override
    public void syncAllUnsafe(String... ids) throws RestCallException {
        syncAllUnsafe(null, ids);
    }

    @Override
    public void syncAllUnsafe(Response<List<E>> response, String... ids) throws RestCallException {
        log("all", ids.length);
        requestHandler.fireRestRequest(getSyncAllRestRequest(ids), getSyncAllResponse(response, ids));
    }

    private void log(String amount, int ids) {
        Log.d(TAG, String.format("Account %s: syncing  %s %s's with %d ids specified", account.getName(), amount, clazz.getSimpleName(), ids));
    }
}
