package org.ovirt.mobile.movirt.util;

import android.content.Context;

/**
 * Created by suomiy on 12/10/15.
 */
public class DateUtils {
    private static final String STRING_UNKNOWN_TIME = "unknown";
    private static final int FORMAT_FLAGS = android.text.format.DateUtils.FORMAT_SHOW_TIME |
            android.text.format.DateUtils.FORMAT_SHOW_DATE |
            android.text.format.DateUtils.FORMAT_ABBREV_MONTH |
            android.text.format.DateUtils.FORMAT_SHOW_YEAR;

    public static final long UNKNOWN_TIME = -1;

    public static String convertDateToString(Context context, long date) {
        if (date == UNKNOWN_TIME) {
            return STRING_UNKNOWN_TIME;
        }
        return android.text.format.DateUtils.formatDateTime(context, date, FORMAT_FLAGS);
    }
}
