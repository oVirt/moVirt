package org.ovirt.mobile.movirt.model.condition;

import android.content.Context;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.enums.VmStatus;

public class VmStatusCondition extends Condition<Vm> {
    private final VmStatus status;

    @JsonCreator
    public VmStatusCondition(@JsonProperty("status") VmStatus status) {
        this.status = status;
    }

    @Override
    public boolean evaluate(Vm entity) {
        return entity.getStatus() == getStatus();
    }

    @Override
    public String getMessage(Context context, Vm vm) {
        return context.getResources().getString(R.string.vm_status_message, vm.getName(), vm.getStatus().toString());
    }

    @Override
    public String toString() {
        return "VM Status is " + getStatus().toString();
    }

    public VmStatus getStatus() {
        return status;
    }
}
