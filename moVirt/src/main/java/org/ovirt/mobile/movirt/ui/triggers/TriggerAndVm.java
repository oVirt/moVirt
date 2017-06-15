package org.ovirt.mobile.movirt.ui.triggers;

import org.ovirt.mobile.movirt.model.Vm;

class TriggerAndVm {
    final Vm entity;
    final SelectedTrigger selectedTrigger;

    TriggerAndVm(Vm entity, SelectedTrigger selectedTrigger) {
        this.entity = entity;
        this.selectedTrigger = selectedTrigger;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TriggerAndVm)) return false;

        TriggerAndVm triggerAndEntity = (TriggerAndVm) o;

        if (entity != null ? !entity.equals(triggerAndEntity.entity) : triggerAndEntity.entity != null)
            return false;
        return selectedTrigger != null ? selectedTrigger.equals(triggerAndEntity.selectedTrigger) : triggerAndEntity.selectedTrigger == null;
    }

    @Override
    public int hashCode() {
        int result = entity != null ? entity.hashCode() : 0;
        result = 31 * result + (selectedTrigger != null ? selectedTrigger.hashCode() : 0);
        return result;
    }
}
