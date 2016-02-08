package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;

import com.j256.ormlite.field.DatabaseField;

import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import static org.springframework.util.StringUtils.isEmpty;

public abstract class SnapshotEmbeddableEntity extends OVirtEntity implements OVirtContract.SnapshotEmbeddableEntity {

    protected SnapshotEmbeddableEntity() {
        snapshotId = "";
    }

    @DatabaseField(columnName = SNAPSHOT_ID, uniqueCombo = true)
    private String snapshotId;

    /**
     * @return not empty snapshotId
     * @throws IllegalStateException if ids of this object aren't initialized
     */
    public String getSnapshotId() {
        if (isEmpty(snapshotId) || isEmpty(super.getId())) {
            throw new IllegalStateException("snapshotId or id isn't set!");
        }
        return snapshotId;
    }

    /**
     * @return true if this objects belongs to some snapshot
     */
    public boolean isSnapshotEmbedded() {
        return !isEmpty(snapshotId);
    }

    /**
     * Sets snapshotId if it isn't initialized yet, id of this object is altered when all ids are initialized
     *
     * @throws IllegalStateException if snapshotId is already initialized
     */
    public synchronized void setSnapshotId(String snapshotId) {
        if (!isEmpty(this.snapshotId)) {
            throw new IllegalStateException("snapshotId is already set!");
        }
        String id = super.getId();
        updateId(null, snapshotId, id, id);
        this.snapshotId = snapshotId;
    }

    /**
     * @return not empty id
     * @throws IllegalStateException if id of this object isn't initialized
     */
    // we have to allow getting id if snapshotId isn't set because not all instances of
    // SnapshotEmbeddableEntity have snapshotId (i.e. they are not embedded in snapshot)
    @Override
    public String getId() {
        String id = super.getId();
        if (isEmpty(id)) {
            throw new IllegalStateException("id isn't set!");
        }
        return id;
    }

    /**
     * Sets id if it isn't initialized yet and is also altered when all ids are initialized
     *
     * @throws IllegalStateException if id is already initialized
     */
    @Override
    public synchronized void setId(String id) {
        if (!isEmpty(super.getId())) {
            throw new IllegalStateException("id is already set!");
        }
        updateId(snapshotId, snapshotId, null, id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SnapshotEmbeddableEntity)) return false;
        if (!super.equals(o)) return false;

        SnapshotEmbeddableEntity that = (SnapshotEmbeddableEntity) o;

        if (!ObjectUtils.equals(snapshotId, that.snapshotId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + snapshotId.hashCode();
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = super.toValues();
        values.put(SNAPSHOT_ID, snapshotId);

        return values;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);
        snapshotId = cursorHelper.getString(SNAPSHOT_ID);
    }

    /**
     * Appends snapshotId to id if both are set
     * This method should be called only in init phase (i.e. oldSnapshotId or oldId are empty)
     *
     * @throws IllegalStateException if oldSnapshotId and oldId aren't empty
     */
    private void updateId(String oldSnapshotId, String snapshotId, String oldId, String id) {
        if (isEmpty(oldSnapshotId) || isEmpty(oldId)) {
            String setValue = "";
            if (!isEmpty(id)) {
                setValue = !isEmpty(snapshotId) ? id + snapshotId : id;
            }
            super.setId(setValue);
        } else {
            throw new UnsupportedOperationException("oldSnapshotId and oldId aren't empty!");
        }
    }
}
