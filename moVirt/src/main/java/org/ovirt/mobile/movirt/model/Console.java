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

    @DatabaseField(columnName = ADDRESS)
    private String address;

    @DatabaseField(columnName = DISPLAY_TYPE)
    private Display displayType;

    @DatabaseField(columnName = PORT)
    private int port;

    @DatabaseField(columnName = TLS_PORT)
    private int tlsPort;

    @DatabaseField(columnName = VM_ID, canBeNull = false)
    private String vmId;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Display getDisplayType() {
        return displayType;
    }

    public void setDisplayType(Display displayType) {
        this.displayType = displayType;
    }

    public String getProtocol() {
        return getDisplayType().getProtocol();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTlsPort() {
        return tlsPort;
    }

    public void setTlsPort(int tlsPort) {
        this.tlsPort = tlsPort;
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


        if (!ObjectUtils.equals(address, console.address)) return false;
        if (displayType != console.displayType) return false;
        if (port != console.port) return false;
        if (tlsPort != console.tlsPort) return false;
        if (!ObjectUtils.equals(vmId, console.vmId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();

        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (displayType != null ? displayType.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + tlsPort;
        result = 31 * result + (vmId != null ? vmId.hashCode() : 0);

        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues contentValues = super.toValues();
        contentValues.put(ADDRESS, getAddress());
        contentValues.put(DISPLAY_TYPE, getDisplayType().toString());
        contentValues.put(PORT, getPort());
        contentValues.put(TLS_PORT, getTlsPort());
        contentValues.put(VM_ID, getVmId());

        return contentValues;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);

        setAddress(cursorHelper.getString(ADDRESS));
        setDisplayType(cursorHelper.getEnum(DISPLAY_TYPE, Display.class));
        setPort(cursorHelper.getInt(PORT));
        setTlsPort(cursorHelper.getInt(TLS_PORT));
        setVmId(cursorHelper.getString(VM_ID));
    }
}
