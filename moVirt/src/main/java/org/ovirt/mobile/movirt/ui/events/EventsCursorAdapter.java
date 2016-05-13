package org.ovirt.mobile.movirt.ui.events;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.provider.OVirtContract;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Event.SEVERITY;

public class EventsCursorAdapter extends SimpleCursorAdapter {
    public EventsCursorAdapter(Context context) {
        super(context,
                R.layout.event_list_item,
                null,
                new String[]{OVirtContract.Event.SEVERITY, OVirtContract.Event.TIME, OVirtContract.Event.DESCRIPTION},
                new int[]{R.id.event_severity, R.id.event_timestamp, R.id.event_description}, 0);

        setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == cursor.getColumnIndex(OVirtContract.Event.TIME)) {
                    TextView textView = (TextView) view;
                    String time = cursor.getString(cursor.getColumnIndex(OVirtContract.Event.TIME));
                    textView.setText(time);
                } else if (columnIndex == cursor.getColumnIndex(OVirtContract.Event.DESCRIPTION)) {
                    TextView textView = (TextView) view;
                    String description = cursor.getString(cursor.getColumnIndex(OVirtContract.Event.DESCRIPTION));
                    textView.setText(description);
                } else if (columnIndex == cursor.getColumnIndex(SEVERITY)) {
                    String severity = cursor.getString(cursor.getColumnIndex(SEVERITY));
                    if (severity != null) {
                        ImageView imageView = (ImageView) view;
                        Event.Severity status = Event.Severity.valueOf(severity);
                        imageView.setImageResource(status.getResource());
                    }
                }

                return true;
            }
        });
    }
}
