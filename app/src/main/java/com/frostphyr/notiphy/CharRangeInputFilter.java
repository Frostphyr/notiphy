package com.frostphyr.notiphy;

import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;

public class CharRangeInputFilter implements InputFilter {

    private char[][] ranges;

    public CharRangeInputFilter(char[][] ranges) {
        this.ranges = ranges;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        boolean modified = false;
        StringBuilder builder = new StringBuilder(end - start);
        for (int i = start; i < end; i++) {
            char c = source.charAt(i);
            if (CharUtils.inRanges(ranges, c)) {
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

}
