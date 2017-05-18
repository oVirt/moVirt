package org.ovirt.mobile.movirt.facade;

import android.os.RemoteException;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.properties.property.version.support.VersionSupport;
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

    public DiskFacade() {
        super(Disk.class);
    }

    @Override
    protected Request<List<Disk>> getSyncAllRestRequest(String... ids) {
        String vmId = ids.length > 0 ? ids[0] : null; // null == all disks

        return oVirtClient.getDisksRequest(vmId);
    }

    @Override
    protected CompositeResponse<List<Disk>> getSyncAllResponse(final Response<List<Disk>> response, final String... ids) {
        switch (ids.length) {
            case 1:// partial sync
                VersionSupport.VM_DISKS.throwIfNotSupported(propertiesManager.getApiVersion());
                final String vmId = ids[0];

                return new CompositeResponse<List<Disk>>()
                        // emulate attachments in version 3
                        .addResponse(new SimpleResponse<List<Disk>>() {
                            @Override
                            public void onResponse(List<Disk> entities) throws RemoteException {
                                List<DiskAttachment> attachments = convertAndClearVmIds(entities);

                                respond(DiskAttachment.class)
                                        .withScopePredicate(new VmIdPredicate<>(vmId))
                                        .updateEntities(attachments);
                            }
                        })
                        .addResponse(respond()
                                .doNotRemoveExpired() // partial
                                .asUpdateEntitiesResponse())
                        .addResponse(response);

            case 0:
                return respond().asUpdateEntitiesResponse()
                        .addResponse(response);
            default:
                throw new UnsupportedOperationException("Unsupported number of Ids");
        }
    }

    private static List<DiskAttachment> convertAndClearVmIds(List<Disk> disks) {
        List<DiskAttachment> attachments = new ArrayList<>();
        for (Disk disk : disks) {
            attachments.add(org.ovirt.mobile.movirt.rest.dto.v4.DiskAttachment.fromV3Disk(disk));
            disk.setVmId(null); // transient
        }

        return attachments;
    }
}
