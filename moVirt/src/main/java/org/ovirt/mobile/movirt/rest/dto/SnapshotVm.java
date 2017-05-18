package org.ovirt.mobile.movirt.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.rest.RestEntityWrapper;
import org.ovirt.mobile.movirt.rest.dto.common.HasId;
import org.ovirt.mobile.movirt.rest.dto.common.VmCpu;
import org.ovirt.mobile.movirt.rest.dto.common.VmOs;
import org.ovirt.mobile.movirt.util.IdHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SnapshotVm implements RestEntityWrapper<org.ovirt.mobile.movirt.model.SnapshotVm>, HasId {

    // public for json mapping
    public String id;
    public String name;
    public String memory;
    public VmOs os;
    public VmCpu cpu;

    public transient String snapshotId;

    @Override
    public String getId() {
        return id;
    }

    public org.ovirt.mobile.movirt.model.SnapshotVm toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.SnapshotVm vm = new org.ovirt.mobile.movirt.model.SnapshotVm();

        vm.setSnapshotId(snapshotId);
        vm.setVmId(IdHelper.combinedId(accountId, id));
        vm.setIds(accountId, IdHelper.combinedId(vm.getSnapshotId(), vm.getVmId())); // make unique id

        vm.setName(name);
        vm.setMemorySize(ObjectUtils.parseLong(memory));
        vm.setSockets(VmCpu.socketsOrDefault(cpu));
        vm.setCoresPerSocket(VmCpu.coresPerSocketOrDefault(cpu));

        if (os != null) {
            vm.setOsType(os.type);
        }

        return vm;
    }
}
