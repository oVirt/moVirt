package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.format.DateUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;

/**
 * Class used to store connection information in database
 * Created by Nika on 23.06.2015.
 */
@DatabaseTable(tableName = ConnectionInfo.TABLE)
public class ConnectionInfo extends BaseEntity<Integer> implements OVirtContract.ConnectionInfo {
    private static final String STRING_UNKNOWN_TIME = "unknown";
    private static final long LONG_UNKNOWN_TIME = -1;
    private static final int FORMAT_FLAGS = DateUtils.FORMAT_SHOW_TIME |
            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_YEAR;
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
        this.lastAttempt = LONG_UNKNOWN_TIME;
        this.lastSuccessful = LONG_UNKNOWN_TIME;
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
        setState(State.valueOf(cursorHelper.getString(STATE)));
        setLastAttempt(cursorHelper.getLong(ATTEMPT));
        setLastSuccessful(cursorHelper.getLong(SUCCESSFUL));
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
        return "Connection: " + state +
                ".\nLast Attempt: " + getLastAttemptWithTimeZone(context) +
                ".\nLast Successful: " + getLastSuccessfulWithTimeZone(context) +
                '.';
    }

    public String getLastAttemptWithTimeZone(Context context) {
        return convertDateToString(context, lastAttempt);
    }

    public String getLastSuccessfulWithTimeZone(Context context) {
        return convertDateToString(context, lastSuccessful);
    }

    private String convertDateToString(Context context, long date) {
        if (date == LONG_UNKNOWN_TIME) {
            return STRING_UNKNOWN_TIME;
        }
        return DateUtils.formatDateTime(context, date, FORMAT_FLAGS);
    }

    public enum State {
        OK,
        FAILED,
        UNKNOWN
    }
}
