package com.frostphyr.notiphy.io;

import android.text.Spanned;
import android.text.SpannedString;

import androidx.room.TypeConverter;

import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.Media;
import com.google.gson.Gson;

import java.util.Date;

public class Converters {

    @TypeConverter
    public static Media stringToMedia(String json) {
        return json == null ? null : new Gson().fromJson(json, Media.class);
    }

    @TypeConverter
    public static String mediaToString(Media media) {
        return media == null ? null : new Gson().toJson(media);
    }

    @TypeConverter
    public static EntryType stringToEntryType(String s) {
        return s == null ? null : EntryType.valueOf(s);
    }

    @TypeConverter
    public static String entryTypeToString(EntryType type) {
        return type == null ? null : type.toString();
    }

    @TypeConverter
    public static Spanned stringToSpanned(String s) {
        return s == null ? null : new SpannedString(s);
    }

    @TypeConverter
    public static String spannedToString(Spanned spanned) {
        return spanned == null ? null : spanned.toString();
    }

    @TypeConverter
    public static Date longToDate(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToLong(Date date) {
        return date == null ? null : date.getTime();
    }

}
