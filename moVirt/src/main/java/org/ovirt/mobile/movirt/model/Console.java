package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Console.TABLE;

@DatabaseTable(tableName = TABLE)
public class Console extends OVirtEntity implements OVirtContract.Console {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    @DatabaseField(columnName = PROTOCOL)
    private ConsoleProtocol protocol;

    @DatabaseField(columnName = VM_ID, canBeNull = false)
    private String vmId;

    public ConsoleProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(ConsoleProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public String getVmId() {
        return vmId;
    }

    @Override
    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Console console = (Console) o;

        if (protocol != console.protocol) return false;
        if (!ObjectUtils.equals(vmId, console.vmId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();

        result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
        result = 31 * result + (vmId != null ? vmId.hashCode() : 0);

        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues contentValues = super.toValues();
        contentValues.put(PROTOCOL, getProtocol().toString());
        contentValues.put(VM_ID, getVmId());

        return contentValues;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);

        setProtocol(cursorHelper.getEnum(PROTOCOL, ConsoleProtocol.class));
        setVmId(cursorHelper.getString(VM_ID));
    }
}
