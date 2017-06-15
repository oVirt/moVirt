package org.ovirt.mobile.movirt.facade;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.DataCenter;
import org.ovirt.mobile.movirt.rest.Request;

import java.util.List;

@EBean
public class DatacenterFacade extends BaseEntityFacade<DataCenter> {

    public DatacenterFacade() {
        super(DataCenter.class);
    }

    @Override
    protected Request<List<DataCenter>> getSyncAllRestRequest(String... ids) {
        return oVirtClient.getDataCentersRequest();
    }
}
