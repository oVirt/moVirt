package org.ovirt.mobile.movirt.model.condition;

import android.content.Context;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Vm;

public class VmMemoryThresholdCondition extends Condition<Vm> {
    private final int percentageLimit;

    @JsonCreator
    public VmMemoryThresholdCondition(@JsonProperty("percentageLimit") int percentageLimit) {
        this.percentageLimit = percentageLimit;
    }

    @Override
    public boolean evaluate(Vm entity) {
        return entity.getMemoryUsage() >= getPercentageLimit();
    }

    @Override
    public String getMessage(Context context, Vm vm) {
        return context.getResources().getString(R.string.vm_memory_message, vm.getName(), getPercentageLimit(), vm.getMemoryUsage());
    }

    @Override
    public String toString() {
        return "VM Memory above " + getPercentageLimit() + "%";
    }

    public int getPercentageLimit() {
        return percentageLimit;
    }
}
