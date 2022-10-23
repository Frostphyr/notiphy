package com.frostphyr.notiphy.io;

import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.frostphyr.notiphy.notification.Message;
import com.frostphyr.notiphy.notification.MessageDao;

@androidx.room.Database(entities = {Message.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class LocalDatabase extends RoomDatabase {

    public abstract MessageDao messageDao();

}
