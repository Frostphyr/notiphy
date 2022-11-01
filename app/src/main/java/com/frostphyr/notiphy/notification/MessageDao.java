package com.frostphyr.notiphy.notification;

import android.content.Context;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Room;

import com.frostphyr.notiphy.io.LocalDatabase;

import java.util.List;

@Dao
public abstract class MessageDao {

    public static MessageDao getInstance(Context context) {
        return Room.databaseBuilder(context, LocalDatabase.class, "local")
                .build().messageDao();
    }

    @Query("SELECT * FROM message ORDER BY timestamp ASC")
    public abstract List<Message> getAll();

    @Query("SELECT * FROM message WHERE url IN (:urls) ORDER BY timestamp ASC")
    public abstract List<Message> getAll(List<String> urls);

    @Insert
    public abstract void add(Message message);

    @Query("DELETE FROM message WHERE url = :url")
    public abstract void delete(String url);

    @Query("DELETE FROM message WHERE url IN (:urls)")
    public abstract void deleteAll(List<String> urls);

}
