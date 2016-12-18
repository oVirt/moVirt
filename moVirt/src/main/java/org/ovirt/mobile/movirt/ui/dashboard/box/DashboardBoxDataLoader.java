package org.ovirt.mobile.movirt.ui.dashboard.box;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import org.ovirt.mobile.movirt.model.base.BaseEntity;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.DataCenter;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.base.SnapshotEmbeddableEntity;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.ProviderFacade;

import java.util.ArrayList;
import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.SnapshotEmbeddableEntity.SNAPSHOT_ID;

public class DashboardBoxDataLoader extends AsyncTaskLoader<List<DashboardBoxData>> {

    private ProviderFacade provider;
    private List<DashboardBoxData> boxDataList;

    public DashboardBoxDataLoader(Context context, ProviderFacade provider) {
        super(context);
        this.provider = provider;
    }

    public enum BoxDataEntityClass {
        DATA_CENTER(DataCenter.class),
        CLUSTER(Cluster.class),
        HOST(Host.class),
        STORAGE_DOMAIN(StorageDomain.class),
        VM(Vm.class),
        EVENT(Event.class);

        private final Class<? extends BaseEntity> entityClass;

        BoxDataEntityClass(Class<? extends BaseEntity> entityClass) {
            this.entityClass = entityClass;
        }

        public Class<? extends BaseEntity> getEntityClass() {
            return entityClass;
        }

    }

    @Override
    public List<DashboardBoxData> loadInBackground() {
        List<DashboardBoxData> boxDataList = new ArrayList<>();

        for (BoxDataEntityClass entityClass : BoxDataEntityClass.values()) {
            final Class<? extends BaseEntity> clazz = entityClass.getEntityClass();

            ProviderFacade.QueryBuilder query = provider.query(clazz);
            if (SnapshotEmbeddableEntity.class.isAssignableFrom(clazz)) {
                query.empty(SNAPSHOT_ID);
            }

            DashboardBoxData boxData = new DashboardBoxData(entityClass);
            boxData.setData(query.all());
            boxDataList.add(boxData);
        }

        return boxDataList;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override
    public void deliverResult(List<DashboardBoxData> data) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (data != null) {
                onReleaseResources(data);
            }
        }
        List<DashboardBoxData> oldData = boxDataList;
        boxDataList = data;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(data);
        }

        // At this point we can release the resources associated with
        // 'oldData' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldData != null) {
            onReleaseResources(oldData);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        if (boxDataList != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(boxDataList);
        }

        if (takeContentChanged() || boxDataList == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override
    public void onCanceled(List<DashboardBoxData> data) {
        super.onCanceled(data);

        // At this point we can release the resources associated with 'data'
        // if needed.
        onReleaseResources(data);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'boxDataList'
        // if needed.
        if (boxDataList != null) {
            onReleaseResources(boxDataList);
            boxDataList = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(List<DashboardBoxData> data) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }
}
