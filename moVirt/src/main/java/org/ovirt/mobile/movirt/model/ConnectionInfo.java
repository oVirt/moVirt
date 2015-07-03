package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;

import java.sql.Timestamp;

/**
 * Class used to store connection information in database
 * Created by Nika on 23.06.2015.
 */
@DatabaseTable(tableName = ConnectionInfo.TABLE)
public class ConnectionInfo extends BaseEntity<Integer> implements OVirtContract.ConnectionInfo {
    @DatabaseField(columnName = ID, id = true)
    private int id;
    @DatabaseField(columnName = ConnectionInfo.STATE)
    private State state;
    @DatabaseField(columnName = ConnectionInfo.ATTEMPT)
    private Timestamp lastAttempt;
    @DatabaseField(columnName = ConnectionInfo.SUCCESSFUL, canBeNull = true)
    private Timestamp lastSuccessful;

    public ConnectionInfo() {
        this.id = 1;
    }

    public void update(State state, Timestamp date) {
        this.state = state;
        this.lastAttempt = date;
        if (state == State.OK) {
            this.lastSuccessful = date;
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
        values.put(ATTEMPT, lastAttempt.toString());
        if (lastSuccessful != null ) {
            values.put(SUCCESSFUL, lastSuccessful.toString());
        } else {
            values.put(SUCCESSFUL, (String) null);
        }
        return values;
    }

    @Override
    protected void initFromCursorHelper(CursorHelper cursorHelper) {
        setId(cursorHelper.getInt(ID));
        setState(State.valueOf(cursorHelper.getString(STATE)));
        setLastAttempt(cursorHelper.getTimestamp(ATTEMPT));
        String success = cursorHelper.getString(SUCCESSFUL);
        if (success != null) {
            setLastSuccessful(Timestamp.valueOf(success));
        }
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Timestamp getLastAttempt() {
        return lastAttempt;
    }

    public void setLastAttempt(Timestamp lastAttempt) {
        this.lastAttempt = lastAttempt;
    }

    public Timestamp getLastSuccessful() {
        return lastSuccessful;
    }

    public void setLastSuccessful(Timestamp lastSuccessful) {
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

    public enum State {
        OK,
        FAILED
    }
}
