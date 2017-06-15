package org.ovirt.mobile.movirt.model.trigger;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.base.BaseEntity;
import org.ovirt.mobile.movirt.model.condition.Condition;
import org.ovirt.mobile.movirt.model.condition.ConditionPersister;
import org.ovirt.mobile.movirt.model.mapping.EntityType;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.HasDisplayResourceId;
import org.ovirt.mobile.movirt.util.JsonUtils;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Trigger.TABLE;

@DatabaseTable(tableName = TABLE)
public class Trigger extends BaseEntity<Integer> implements OVirtContract.Trigger {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    public enum NotificationType implements HasDisplayResourceId {
        INFO(R.string.notification_type_info),
        CRITICAL(R.string.notification_type_critical);

        private final int displayResource;

        NotificationType(int displayResource) {
            this.displayResource = displayResource;
        }

        @Override
        public int getDisplayResourceId() {
            return displayResource;
        }
    }

    @DatabaseField(columnName = ID, generatedId = true, allowGeneratedIdInsert = true)
    private int id;

    @DatabaseField(columnName = ACCOUNT_ID, canBeNull = true)
    private String accountId;

    @DatabaseField(columnName = CLUSTER_ID, canBeNull = true)
    private String clusterId;

    @DatabaseField(columnName = TARGET_ID, canBeNull = true)
    private String targetId;

    @DatabaseField(columnName = NOTIFICATION, canBeNull = false)
    private NotificationType notificationType;

    @DatabaseField(columnName = CONDITION, canBeNull = false, persisterClass = ConditionPersister.class)
    private Condition condition;

    @DatabaseField(columnName = ENTITY_TYPE, canBeNull = false)
    private EntityType entityType;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
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

    public <E extends BaseEntity<?>> Condition<E> getCondition() {
        return condition;
    }

    public <E extends BaseEntity<?>> void setCondition(Condition<E> condition) {
        this.condition = condition;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    @Override
    public ContentValues toValues() {
        ContentValues contentValues = new ContentValues();
        if (id != 0) {
            contentValues.put(ID, id);
        }
        contentValues.put(ACCOUNT_ID, accountId);
        contentValues.put(CLUSTER_ID, clusterId);
        contentValues.put(TARGET_ID, targetId);
        contentValues.put(NOTIFICATION, notificationType.toString());
        contentValues.put(CONDITION, JsonUtils.objectToString(condition));
        contentValues.put(ENTITY_TYPE, entityType.toString());
        return contentValues;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        setId(cursorHelper.getInt(OVirtContract.Trigger.ID));
        setAccountId(cursorHelper.getString(OVirtContract.Trigger.ACCOUNT_ID));
        setClusterId(cursorHelper.getString(OVirtContract.Trigger.CLUSTER_ID));
        setTargetId(cursorHelper.getString(OVirtContract.Trigger.TARGET_ID));
        setNotificationType(cursorHelper.getEnum(OVirtContract.Trigger.NOTIFICATION, Trigger.NotificationType.class));
        setCondition(cursorHelper.getJson(OVirtContract.Trigger.CONDITION, Condition.class));
        setEntityType(cursorHelper.getEnum(OVirtContract.Trigger.ENTITY_TYPE, EntityType.class));
    }

    @Override
    public String toString() {
        return condition == null ? "" : condition.toString();
    }
}
