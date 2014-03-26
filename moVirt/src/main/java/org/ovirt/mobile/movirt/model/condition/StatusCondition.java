package org.ovirt.mobile.movirt.model.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Vm;

public class StatusCondition extends Condition<Vm> {
    public final Vm.Status status;

    @JsonCreator
    public StatusCondition(@JsonProperty("status") Vm.Status status) {
        this.status = status;
    }

    @Override
    public boolean evaluate(Vm entity) {
        return entity.getStatus() == status;
    }

    @Override
    public String getMessage(Vm vm) {
        return getResources().getString(R.string.vm_status_message, vm.getName(), vm.getStatus().toString());
    }

    @Override
    public String toString() {
        return "Status is " + status.toString();
    }
}
