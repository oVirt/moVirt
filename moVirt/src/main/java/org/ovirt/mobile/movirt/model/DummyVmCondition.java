package org.ovirt.mobile.movirt.model;

public class DummyVmCondition extends Condition<Vm> {
    @Override
    public boolean evaluate(Vm oldEntity, Vm newEntity) {
       // return (oldEntity.getStatus() == Vm.Status.UP && newEntity.getStatus() == Vm.Status.DOWN);
        return !oldEntity.getName().equals(newEntity.getName());
    }

    @Override
    public String toString() {
        return "Condition: VM status is 'Down'";
    }
}
