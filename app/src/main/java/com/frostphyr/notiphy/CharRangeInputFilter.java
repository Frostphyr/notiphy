package com.frostphyr.notiphy;

import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;

public class CharRangeInputFilter implements InputFilter {

    private final char[][] ranges;

    public CharRangeInputFilter(char[][] ranges) {
        this.ranges = ranges;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        boolean modified = false;
        StringBuilder builder = new StringBuilder(end - start);
        for (int i = start; i < end; i++) {
            char c = source.charAt(i);
            if (inRanges(ranges, c)) {
                builder.append(c);
            } else {
                modified = true;
            }
        }

        if (modified) {
            if (source instanceof Spanned) {
                SpannableString s = new SpannableString(builder);
                TextUtils.copySpansFrom((Spanned) source, start, builder.length(), null, s, 0);
                return s;
            } else {
                return builder;
            }
        }
        return null;
    }

    public static boolean inRanges(char[][] ranges, char c) {
        for (char[] range : ranges) {
            if (range.length == 1) {
                if (c == range[0]) {
                    return true;
                }
            } else if (range.length == 2) {
                if (c >= range[0] && c <= range[1]) {
                    return true;
                }
            }
        }
        return false;
    }

}
