package com.frostphyr.notiphy.io;

import android.content.Context;
import android.os.Environment;
import android.support.v4.util.AtomicFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileIO {

    public static byte[] read(Context context, String name) throws IOException {
        File file = getFile(context, name);
        if (file.exists()) {
            AtomicFile atomicFile = new AtomicFile(file);
            return atomicFile.readFully();
        }
        return null;
    }

    public static void write(Context context, String name, byte[] data) throws IOException {
        AtomicFile file = new AtomicFile(getFile(context, name));
        FileOutputStream out = file.startWrite();
        out.write(data);
        file.finishWrite(out);
    }

    private static File getFile(Context context, String name) {
        File dir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dir = context.getExternalFilesDir(null);
        } else {
            dir =  context.getFilesDir();
        }
        return new File(dir, name);
    }

}
