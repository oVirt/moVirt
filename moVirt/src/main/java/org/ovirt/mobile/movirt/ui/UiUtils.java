package org.ovirt.mobile.movirt.ui;

import android.support.annotation.NonNull;
import android.widget.MultiAutoCompleteTextView;

public class UiUtils {

    public static int addAlphaToColor(int color, float opacity) {
        if (opacity > 1 || opacity < 0) {
            throw new IllegalArgumentException("opacity should be in interval 0-1");
        }

        return ((int) (opacity * 255.0f) << 24) | (color & 0x00ffffff);
    }

    @NonNull
    public static MultiAutoCompleteTextView.Tokenizer getUrlTokenizer() {
        return new MultiAutoCompleteTextView.Tokenizer() {
            @Override
            public int findTokenStart(CharSequence text, int cursor) {
                int i = cursor;
                while (i > 0 && text.charAt(i - 1) != '/' && text.charAt(i - 1) != ':') {
                    i--;
                }
                return i;
            }

            @Override
            public int findTokenEnd(CharSequence text, int cursor) {
                int i = cursor;
                int len = text.length();
                while (i < len) {
                    if (text.charAt(i) == '/') {
                        return i;
                    } else {
                        i++;
                    }
                }
                return len;
            }

            @Override
            public CharSequence terminateToken(CharSequence text) {
                return text;
            }
        };
    }

    @NonNull
    public static MultiAutoCompleteTextView.Tokenizer getUsernameTokenizer() {
        return new MultiAutoCompleteTextView.Tokenizer() {
            @Override
            public int findTokenStart(CharSequence text, int cursor) {
                int i = cursor;
                while (i > 0 && text.charAt(i - 1) != '@') {
                    i--;
                }
                return i;
            }

            @Override
            public int findTokenEnd(CharSequence text, int cursor) {
                int i = cursor;
                int len = text.length();
                while (i < len) {
                    if (text.charAt(i) == '@') {
                        return i;
                    } else {
                        i++;
                    }
                }
                return len;
            }

            @Override
            public CharSequence terminateToken(CharSequence text) {
                return text;
            }
        };
    }
}
