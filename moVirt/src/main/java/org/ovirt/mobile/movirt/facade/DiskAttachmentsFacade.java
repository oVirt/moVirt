package org.ovirt.mobile.movirt.facade;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.properties.property.version.support.VersionSupport;
import org.ovirt.mobile.movirt.facade.predicates.VmIdPredicate;
import org.ovirt.mobile.movirt.model.DiskAttachment;
import org.ovirt.mobile.movirt.rest.CompositeResponse;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.Response;

import java.util.List;

import static org.ovirt.mobile.movirt.util.ObjectUtils.requireSignature;

@EBean
public class DiskAttachmentsFacade extends BaseEntityFacade<DiskAttachment> {

    public DiskAttachmentsFacade() {
        super(DiskAttachment.class);
    }

    @Override
    protected Request<List<DiskAttachment>> getSyncAllRestRequest(String... ids) {
        requireSignature(ids, "vmId");
        String vmId = ids[0];
        return oVirtClient.getDisksAttachmentsRequest(vmId);
    }

    @Override
    protected CompositeResponse<List<DiskAttachment>> getSyncAllResponse(final Response<List<DiskAttachment>> response, final String... ids) {
        requireSignature(ids, "vmId");
        String vmId = ids[0];
        VersionSupport.DISK_ATTACHMENTS.throwIfNotSupported(propertiesManager.getApiVersion());

        return respond().withScopePredicate(new VmIdPredicate<>(vmId))
                .asUpdateEntitiesResponse()
                .addResponse(response);
    }
}
