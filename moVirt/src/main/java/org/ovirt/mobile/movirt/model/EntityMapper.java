package org.ovirt.mobile.movirt.model;

import android.database.Cursor;

import org.ovirt.mobile.movirt.model.condition.Condition;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;

import java.util.HashMap;
import java.util.Map;

public abstract class EntityMapper<E> {
    public abstract E fromCursor(Cursor cursor);

    private static final Map<Class<?>, EntityMapper<?>> mappers = new HashMap<>();

    public static final EntityMapper<Vm> VM_MAPPER = new EntityMapper<Vm>() {
        @Override
        public Vm fromCursor(Cursor cursor) {
            CursorHelper cursorHelper = new CursorHelper(cursor);
            Vm vm = new Vm();
            vm.setId(cursorHelper.getString(OVirtContract.Vm.ID));
            vm.setName(cursorHelper.getString(OVirtContract.Vm.NAME));
            vm.setStatus(cursorHelper.getEnum(OVirtContract.Vm.STATUS, Vm.Status.class));
            vm.setClusterId(cursorHelper.getString(OVirtContract.Vm.CLUSTER_ID));
            vm.setCpuUsage(cursorHelper.getDouble(OVirtContract.Vm.CPU_USAGE));
            vm.setMemoryUsage(cursorHelper.getDouble(OVirtContract.Vm.MEMORY_USAGE));
            return vm;
        }
    };

    public static final EntityMapper<Cluster> CLUSTER_MAPPER = new EntityMapper<Cluster>() {
        @Override
        public Cluster fromCursor(Cursor cursor) {
            Cluster cluster = new Cluster();
            cluster.setId(cursor.getString(cursor.getColumnIndex(OVirtContract.Cluster.ID)));
            cluster.setName(cursor.getString(cursor.getColumnIndex(OVirtContract.Cluster.NAME)));
            return cluster;
        }
    };

    public static final EntityMapper<Trigger<?>> TRIGGER_MAPPER = new EntityMapper<Trigger<?>>() {
        @Override
        public Trigger<?> fromCursor(Cursor cursor) {
            CursorHelper cursorHelper = new CursorHelper(cursor);
            Trigger<?> trigger = new Trigger<>();
            trigger.setId(cursorHelper.getInt(OVirtContract.Trigger.ID));
            trigger.setNotificationType(cursorHelper.getEnum(OVirtContract.Trigger.NOTIFICATION, Trigger.NotificationType.class));
            trigger.setCondition(cursorHelper.getJson(OVirtContract.Trigger.CONDITION, Condition.class));
            trigger.setScope(cursorHelper.getEnum(OVirtContract.Trigger.SCOPE, Trigger.Scope.class));
            trigger.setTargetId(cursorHelper.getString(OVirtContract.Trigger.TARGET_ID));
            trigger.setEntityType(cursorHelper.getEnum(OVirtContract.Trigger.ENTITY_TYPE, EntityType.class));
            return trigger;
        }
    };

    static {
        mappers.put(Vm.class, VM_MAPPER);
        mappers.put(Cluster.class, CLUSTER_MAPPER);
        mappers.put(Trigger.class, TRIGGER_MAPPER);
    }

    @SuppressWarnings("unchecked")
    public static <E> EntityMapper<E> forEntity(Class<E> clazz) {
        return (EntityMapper<E>) mappers.get(clazz);
    }
}
