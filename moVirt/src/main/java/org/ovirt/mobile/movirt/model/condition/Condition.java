package org.ovirt.mobile.movirt.model.condition;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.ovirt.mobile.movirt.model.OVirtEntity;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(CpuThresholdCondition.class),
               @JsonSubTypes.Type(MemoryThresholdCondition.class),
               @JsonSubTypes.Type(StatusCondition.class)})
public abstract class Condition<T extends OVirtEntity> {
    public abstract boolean evaluate(T entity);
}
