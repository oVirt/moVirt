package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.DateUtils;

/**
 * Class used to store connection information in database
 * Created by Nika on 23.06.2015.
 */
@DatabaseTable(tableName = ConnectionInfo.TABLE)
public class ConnectionInfo extends BaseEntity<Integer> implements OVirtContract.ConnectionInfo {
    private static final long UNKNOWN_TIME = DateUtils.UNKNOWN_TIME;

    @DatabaseField(columnName = ID, id = true)
    private int id;
    @DatabaseField(columnName = ConnectionInfo.STATE)
    private State state;
    @DatabaseField(columnName = ConnectionInfo.ATTEMPT)
    private long lastAttempt;
    @DatabaseField(columnName = ConnectionInfo.SUCCESSFUL)
    private long lastSuccessful;

    public ConnectionInfo() {
        this.id = 1;
        this.state = State.UNKNOWN;
        this.lastAttempt = UNKNOWN_TIME;
        this.lastSuccessful = UNKNOWN_TIME;
    }

    public void updateWithCurrentTime(State state) {
        this.state = state;
        long time = System.currentTimeMillis();
        this.lastAttempt = time;
        if (state == State.OK) {
            this.lastSuccessful = time;
        }
    }

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ID, id);
        values.put(STATE, state.toString());
        values.put(ATTEMPT, lastAttempt);
        values.put(SUCCESSFUL, lastSuccessful);
        return values;
    }

    @Override
    protected void initFromCursorHelper(CursorHelper cursorHelper) {
        setId(cursorHelper.getInt(ID));
        try {
            setState(State.valueOf(cursorHelper.getString(STATE)));
        } catch (Exception e) {
            setState(State.UNKNOWN);
        }
        try {
            setLastAttempt(cursorHelper.getLong(ATTEMPT));
        } catch (Exception e) {
            setLastAttempt(UNKNOWN_TIME);
        }
        try {
            setLastSuccessful(cursorHelper.getLong(SUCCESSFUL));
        } catch (Exception e) {
            setLastSuccessful(UNKNOWN_TIME);
        }
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public long getLastAttempt() {
        return lastAttempt;
    }

    public void setLastAttempt(long lastAttempt) {
        this.lastAttempt = lastAttempt;
    }

    public long getLastSuccessful() {
        return lastSuccessful;
    }

    public void setLastSuccessful(long lastSuccessful) {
        this.lastSuccessful = lastSuccessful;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage(Context context) {
        return "Connection: " + state.toString().replace('_', ' ') +
                ".\nLast Attempt: " + getLastAttemptWithTimeZone(context) +
                ".\nLast Successful: " + getLastSuccessfulWithTimeZone(context) +
                '.';
    }

    public String getLastAttemptWithTimeZone(Context context) {
        return DateUtils.convertDateToString(context, lastAttempt);
    }

    public String getLastSuccessfulWithTimeZone(Context context) {
        return DateUtils.convertDateToString(context, lastSuccessful);
    }

    public enum State {
        OK,
        FAILED,
        FAILED_REPEATEDLY,
        UNKNOWN
    }
}
