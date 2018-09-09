package com.frostphyr.notiphy;

public final class CharUtils {

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

}
