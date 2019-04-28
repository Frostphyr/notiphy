package com.frostphyr.notiphy;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

public final class TextUtils {

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

    public static boolean inRanges(char[][] ranges, CharSequence text) {
        for (int i = 0; i < text.length(); i++) {
            if (!inRanges(ranges, text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static Spanned fromHtml(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(html);
        }
    }

    public static String concat(String[] arr, String between) {
        StringBuilder builder = new StringBuilder(Math.max(arr.length * 2 - 1, 0));
        for (int i = 0; i < arr.length; i++) {
            builder.append(arr[i]);
            if (between != null && i != arr.length - 1) {
                builder.append(between);
            }
        }
        return builder.toString();
    }

}
