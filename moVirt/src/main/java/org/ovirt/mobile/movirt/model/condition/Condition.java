package org.ovirt.mobile.movirt.model.condition;

import android.content.Context;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.ovirt.mobile.movirt.model.base.BaseEntity;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(VmCpuThresholdCondition.class),
        @JsonSubTypes.Type(VmMemoryThresholdCondition.class),
        @JsonSubTypes.Type(VmStatusCondition.class),
        @JsonSubTypes.Type(EventCondition.class)})
public abstract class Condition<T extends BaseEntity<?>> {
    public abstract boolean evaluate(T entity);

    public abstract String getMessage(Context context, T entity);
}
