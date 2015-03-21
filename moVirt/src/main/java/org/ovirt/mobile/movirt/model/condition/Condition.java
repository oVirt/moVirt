package org.ovirt.mobile.movirt.model.condition;

import android.content.res.Resources;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.model.BaseEntity;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(CpuThresholdCondition.class),
               @JsonSubTypes.Type(MemoryThresholdCondition.class),
               @JsonSubTypes.Type(StatusCondition.class),
               @JsonSubTypes.Type(EventCondition.class)
               })
public abstract class Condition<T extends BaseEntity<?>> {
    public abstract boolean evaluate(T entity);

    public abstract String getMessage(T entity);

    protected Resources getResources() {
        return MoVirtApp.getContext().getResources();
    }
}
