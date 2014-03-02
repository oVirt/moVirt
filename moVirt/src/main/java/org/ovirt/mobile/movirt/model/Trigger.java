package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.util.JsonUtils;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Trigger.*;

@DatabaseTable(tableName = TABLE)
public class Trigger<E extends OVirtEntity>  {
    public enum Scope {
        GLOBAL,
        CLUSTER,
        ITEM
    }

    public enum NotificationType {
        INFO,
        CRITICAL
    }

    @DatabaseField(columnName = _ID, id = true)
    private int id;

    @DatabaseField(columnName = NOTIFICATION, canBeNull = false)
    private NotificationType notificationType;

    @DatabaseField(columnName = CONDITION, canBeNull = false, persisterClass = ConditionPersister.class)
    private Condition<E> condition;

    @DatabaseField(columnName = SCOPE, canBeNull = false)
    private Scope scope;

    @DatabaseField(columnName = TARGET_ID, canBeNull = true)
    private String targetId;

    @DatabaseField(columnName = ENTITY_TYPE, canBeNull = false)
    private EntityType entityType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public Condition<E> getCondition() {
        return condition;
    }

    public void setCondition(Condition<E> condition) {
        this.condition = condition;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public ContentValues toValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(_ID, id);
        contentValues.put(NOTIFICATION, notificationType.toString());
        contentValues.put(CONDITION, JsonUtils.objectToString(condition));
        contentValues.put(SCOPE, scope.toString());
        contentValues.put(TARGET_ID, targetId);
        contentValues.put(ENTITY_TYPE, entityType.toString());
        return contentValues;
    }
}
