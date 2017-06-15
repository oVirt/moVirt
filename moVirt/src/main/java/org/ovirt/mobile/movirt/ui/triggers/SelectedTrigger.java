package org.ovirt.mobile.movirt.ui.triggers;

import org.ovirt.mobile.movirt.model.trigger.Trigger;

class SelectedTrigger {
    final Trigger trigger;

    SelectedTrigger(Trigger trigger) {
        this.trigger = trigger;
    }

    boolean isSelected() {
        return trigger != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SelectedTrigger)) return false;

        SelectedTrigger that = (SelectedTrigger) o;

        return trigger != null ? trigger.equals(that.trigger) : that.trigger == null;
    }

    @Override
    public int hashCode() {
        return trigger != null ? trigger.hashCode() : 0;
    }
}
