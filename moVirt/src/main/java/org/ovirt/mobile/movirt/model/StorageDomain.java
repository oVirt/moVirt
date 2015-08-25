package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import static org.ovirt.mobile.movirt.provider.OVirtContract.StorageDomain.TABLE;

@DatabaseTable(tableName = TABLE)
public class StorageDomain extends OVirtEntity implements OVirtContract.StorageDomain {

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
        LOCKED(R.drawable.unconfigured),
        MIXED(R.drawable.unconfigured),
        UNATTACHED(R.drawable.unconfigured),
        MAINTENANCE(R.drawable.unconfigured),
        PREPARING_FOR_MAINTENANCE(R.drawable.unconfigured),
        DETACHING(R.drawable.unconfigured),
        ACTIVATING(R.drawable.unconfigured),
        UNKNOWN(R.drawable.unconfigured);

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

    @DatabaseField(columnName = AVAILABLE_SIZE_MB)
    private long availableSizeMb;

    @DatabaseField(columnName = USED_SIZE_MB)
    private long usedSizeMb;

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

    public long getAvailableSizeMb() {
        return availableSizeMb;
    }

    public void setAvailableSizeMb(long availableSizeMb) {
        this.availableSizeMb = availableSizeMb;
    }

    public long getUsedSizeMb() {
        return usedSizeMb;
    }

    public void setUsedSizeMb(long usedSizeMb) {
        this.usedSizeMb = usedSizeMb;
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
        if (availableSizeMb != storageDomain.availableSizeMb) return false;
        if (usedSizeMb != storageDomain.usedSizeMb) return false;
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
        result = 31 * result + (int) (availableSizeMb ^ (availableSizeMb >>> 32));
        result = 31 * result + (int) (usedSizeMb ^ (usedSizeMb >>> 32));
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
        values.put(AVAILABLE_SIZE_MB, getAvailableSizeMb());
        values.put(USED_SIZE_MB, getUsedSizeMb());
        values.put(STATUS, getStatus().toString());
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
        setAvailableSizeMb(cursorHelper.getLong(AVAILABLE_SIZE_MB));
        setUsedSizeMb(cursorHelper.getLong(USED_SIZE_MB));
        setStatus(cursorHelper.getEnum(STATUS, StorageDomain.Status.class));
        setStorageAddress(cursorHelper.getString(STORAGE_ADDRESS));
        setStorageType(cursorHelper.getString(STORAGE_TYPE));
        setStoragePath(cursorHelper.getString(STORAGE_PATH));
        setStorageFormat(cursorHelper.getString(STORAGE_FORMAT));
    }

}
