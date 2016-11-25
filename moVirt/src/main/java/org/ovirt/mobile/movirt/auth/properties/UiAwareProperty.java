package org.ovirt.mobile.movirt.auth.properties;

public class UiAwareProperty<T> {
    private T property;
    private Boolean uiUpdated = false;

    public UiAwareProperty(T newProperty, T OldProperty) {
        this.property = newProperty;
        this.uiUpdated = PropertyUtils.propertyObjectEquals(newProperty, OldProperty);
    }

    /**
     * Constructor for property which is not updated by UI
     *
     * @param property property
     */
    public UiAwareProperty(T property) {
        this.property = property;
    }

    public T getProperty() {
        return property;
    }

    /**
     * @return true if UI was already updated; i.e. change was fired from this UI
     */
    public boolean uiUpdated() {
        return uiUpdated;
    }

    /**
     * @return true if UI was not updated; i.e. change was not fired from this UI
     */
    public boolean uiNotUpdated() {
        return !uiUpdated;
    }
}
