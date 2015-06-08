package org.ovirt.mobile.movirt.ui;

import org.ovirt.mobile.movirt.model.OVirtEntity;

public interface UpdateMenuItemAware<E extends OVirtEntity> {
    void updateMenuItem(E entity);
}
