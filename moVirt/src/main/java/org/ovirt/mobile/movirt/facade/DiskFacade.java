package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import com.android.internal.util.Predicate;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.property.Version;
import org.ovirt.mobile.movirt.auth.properties.property.exceptions.UnsupportedAfterException;
import org.ovirt.mobile.movirt.facade.predicates.NormalDiskPredicate;
import org.ovirt.mobile.movirt.facade.predicates.SnapshotIdPredicate;
import org.ovirt.mobile.movirt.facade.predicates.VmIdPredicate;
import org.ovirt.mobile.movirt.model.Disk;
import org.ovirt.mobile.movirt.model.DiskAttachment;
import org.ovirt.mobile.movirt.rest.CompositeResponse;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.Response;
import org.ovirt.mobile.movirt.rest.SimpleResponse;

import java.util.ArrayList;
import java.util.List;

@EBean
public class DiskFacade extends BaseEntityFacade<Disk> {

    @Bean
    AccountPropertiesManager propertiesManager;

    public DiskFacade() {
        super(Disk.class);
    }

    @Override
    public Intent getDetailIntent(Disk entity, Context context) {
        return null;
    }

    @Override
    protected Request<Disk> getSyncOneRestRequest(String diskId, String... ids) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Request<List<Disk>> getSyncAllRestRequest(String... ids) {
        String vmId = ids.length > 0 ? ids[0] : null;
        String snapshotId = ids.length > 1 ? ids[1] : null;

        return oVirtClient.getDisksRequest(vmId, snapshotId);
    }

    @Override
    protected CompositeResponse<List<Disk>> getSyncAllResponse(final Response<List<Disk>> response, final String... ids) {
        CompositeResponse<List<Disk>> responses = new CompositeResponse<>();

        switch (ids.length) {
            case 2:  // snapshots are special case and they have vmId
                final String snapshotId = ids[1];
                Predicate<Disk> snapshotPredicate = new SnapshotIdPredicate<>(snapshotId);
                responses.addResponse(syncAdapter.getUpdateEntitiesResponse(Disk.class, snapshotPredicate));
                break;
            case 1:// partial sync
                if (propertiesManager.getApiVersion().isV4Api()) {
                    throw new UnsupportedAfterException(Version.V4, "Vm Disks");
                }
                final String vmId = ids[0];
                // emulate attachments in version 3
                responses.addResponse(new SimpleResponse<List<Disk>>() { // emulate first because we delete vmIds from Disks
                    @Override
                    public void onResponse(List<Disk> entities) throws RemoteException {
                        List<DiskAttachment> attachments = convertAndClearVmIds(entities);
                        syncAdapter.updateLocalEntities(attachments, DiskAttachment.class, new VmIdPredicate<DiskAttachment>(vmId));
                    }
                });
                responses.addResponse(syncAdapter.getUpdateEntitiesResponse(Disk.class, new NormalDiskPredicate(), false)); // partial

                break;
            case 0:
                responses.addResponse(syncAdapter.getUpdateEntitiesResponse(Disk.class, new NormalDiskPredicate()));
                break;
            default:
                throw new UnsupportedOperationException("Unsupported number of Ids");
        }

        responses.addResponse(response);
        return responses;
    }

    private static List<DiskAttachment> convertAndClearVmIds(List<Disk> disks) {
        List<DiskAttachment> attachments = new ArrayList<>();
        for (Disk disk : disks) {
            attachments.add(org.ovirt.mobile.movirt.rest.dto.v4.DiskAttachment.fromV3Disk(disk));
            disk.setVmId(null); // important
        }

        return attachments;
    }
}
