package org.ovirt.mobile.movirt.model.base;

import android.content.ContentValues;

import com.j256.ormlite.field.DatabaseField;

import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.IdHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

public abstract class OVirtAccountEntity extends OVirtBaseEntity implements OVirtContract.AccountEntity {

    @DatabaseField(columnName = SHORT_ID, canBeNull = false, uniqueCombo = true)
    private String shortId;

    @DatabaseField(columnName = ACCOUNT_ID, canBeNull = false, uniqueCombo = true)
    private String accountId;

    public void setIds(String accountId, String id) {
        setId(IdHelper.combinedId(accountId, id));
        setAccountId(accountId);
        setShortId(id);
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getShortId() {
        return shortId;
    }

    public void setShortId(String shortId) {
        this.shortId = shortId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OVirtAccountEntity)) return false;
        if (!super.equals(o)) return false;

        OVirtAccountEntity that = (OVirtAccountEntity) o;

        if (!ObjectUtils.equals(shortId, that.shortId)) return false;
        if (!ObjectUtils.equals(accountId, that.accountId)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (shortId != null ? shortId.hashCode() : 0);
        result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = super.toValues();
        values.put(SHORT_ID, getShortId());
        values.put(ACCOUNT_ID, getAccountId());
        return values;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);
        setShortId(cursorHelper.getString(SHORT_ID));
        setAccountId(cursorHelper.getString(ACCOUNT_ID));
    }
}
