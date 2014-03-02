package org.ovirt.mobile.movirt.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(DummyVmCondition.class)})
public abstract class Condition<T extends OVirtEntity> {
    public abstract boolean evaluate(T oldEntity, T newEntity);
}
