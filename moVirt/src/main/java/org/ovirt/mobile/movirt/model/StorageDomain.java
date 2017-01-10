package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.base.OVirtNamedEntity;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import static org.ovirt.mobile.movirt.provider.OVirtContract.StorageDomain.TABLE;

@DatabaseTable(tableName = TABLE)
public class StorageDomain extends OVirtNamedEntity implements OVirtContract.StorageDomain {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    public enum Type {
        DATA,
        ISO,
        IMAGE,
        EXPORT
    }

    public enum Status {
        ACTIVE(R.drawable.up),
        INACTIVE(R.drawable.down),
        LOCKED(R.drawable.lock),
        MIXED(R.drawable.unconfigured),
        UNATTACHED(R.drawable.torn_chain),
        MAINTENANCE(R.drawable.maintenance),
        PREPARING_FOR_MAINTENANCE(R.drawable.lock),
        DETACHING(R.drawable.lock),
        ACTIVATING(R.drawable.lock),
        UNKNOWN(R.drawable.down);

        Status(int resource) {
            this.resource = resource;
        }

        private final int resource;

        public int getResource() {
            return resource;
        }
    }

    @DatabaseField(columnName = TYPE)
    private Type type;

    @DatabaseField(columnName = AVAILABLE_SIZE)
    private long availableSize;

    @DatabaseField(columnName = USED_SIZE)
    private long usedSize;

    @DatabaseField(columnName = STATUS)
    private Status status;

    @DatabaseField(columnName = STORAGE_ADDRESS)
    private String storageAddress;

    @DatabaseField(columnName = STORAGE_TYPE)
    private String storageType;

    @DatabaseField(columnName = STORAGE_PATH)
    private String storagePath;

    @DatabaseField(columnName = STORAGE_FORMAT)
    private String storageFormat;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public long getAvailableSize() {
        return availableSize;
    }

    public void setAvailableSize(long availableSize) {
        this.availableSize = availableSize;
    }

    public long getUsedSize() {
        return usedSize;
    }

    public void setUsedSize(long usedSize) {
        this.usedSize = usedSize;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getStorageAddress() {
        return storageAddress;
    }

    public void setStorageAddress(String storageAddress) {
        this.storageAddress = storageAddress;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getStorageFormat() {
        return storageFormat;
    }

    public void setStorageFormat(String storageFormat) {
        this.storageFormat = storageFormat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        StorageDomain storageDomain = (StorageDomain) o;

        if (type != storageDomain.type) return false;
        if (availableSize != storageDomain.availableSize) return false;
        if (usedSize != storageDomain.usedSize) return false;
        if (status != storageDomain.status) return false;
        if (!ObjectUtils.equals(storageAddress, storageDomain.storageAddress)) return false;
        if (!ObjectUtils.equals(storageType, storageDomain.storageType)) return false;
        if (!ObjectUtils.equals(storagePath, storageDomain.storagePath)) return false;
        if (!ObjectUtils.equals(storageFormat, storageDomain.storageFormat)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (int) (availableSize ^ (availableSize >>> 32));
        result = 31 * result + (int) (usedSize ^ (usedSize >>> 32));
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (storageAddress != null ? storageAddress.hashCode() : 0);
        result = 31 * result + (storageType != null ? storageType.hashCode() : 0);
        result = 31 * result + (storagePath != null ? storagePath.hashCode() : 0);
        result = 31 * result + (storageFormat != null ? storageFormat.hashCode() : 0);

        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = super.toValues();
        values.put(TYPE, getType().toString());
        values.put(AVAILABLE_SIZE, getAvailableSize());
        values.put(USED_SIZE, getUsedSize());
        values.put(STATUS, getStatus() != null ? getStatus().toString() : null);
        values.put(STORAGE_ADDRESS, getStorageAddress());
        values.put(STORAGE_TYPE, getStorageType());
        values.put(STORAGE_PATH, getStoragePath());
        values.put(STORAGE_FORMAT, getStorageFormat());

        return values;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);

        setType(cursorHelper.getEnum(TYPE, StorageDomain.Type.class));
        setAvailableSize(cursorHelper.getLong(AVAILABLE_SIZE));
        setUsedSize(cursorHelper.getLong(USED_SIZE));
        try {
            setStatus(cursorHelper.getEnum(STATUS, StorageDomain.Status.class));
        } catch (Exception e) {
            // ignore, at least we have tried
        }

        setStorageAddress(cursorHelper.getString(STORAGE_ADDRESS));
        setStorageType(cursorHelper.getString(STORAGE_TYPE));
        setStoragePath(cursorHelper.getString(STORAGE_PATH));
        setStorageFormat(cursorHelper.getString(STORAGE_FORMAT));
    }
}
