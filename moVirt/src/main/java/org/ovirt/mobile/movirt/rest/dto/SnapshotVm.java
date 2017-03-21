package org.ovirt.mobile.movirt.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.rest.RestEntityWrapper;
import org.ovirt.mobile.movirt.rest.dto.common.VmCpu;
import org.ovirt.mobile.movirt.rest.dto.common.VmOs;
import org.ovirt.mobile.movirt.util.ObjectUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnapshotVm implements RestEntityWrapper<org.ovirt.mobile.movirt.model.SnapshotVm> {

    // public for json mapping
    public String id;
    public String name;
    public String memory;
    public VmOs os;
    public VmCpu cpu;

    public transient String snapshotId;

    public org.ovirt.mobile.movirt.model.SnapshotVm toEntity() {
        org.ovirt.mobile.movirt.model.SnapshotVm vm = new org.ovirt.mobile.movirt.model.SnapshotVm();

        if (id == null || snapshotId == null) {
            throw new IllegalArgumentException("cannot create composite id");
        }

        vm.setId(id + snapshotId); // make unique id
        vm.setSnapshotId(snapshotId);
        vm.setVmId(id);

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
