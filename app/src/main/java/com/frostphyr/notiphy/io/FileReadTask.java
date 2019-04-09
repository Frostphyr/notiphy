package com.frostphyr.notiphy.io;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import androidx.core.util.AtomicFile;

public abstract class FileReadTask<Params, Result> extends FileIOTask<Params, Result> {

    public FileReadTask(Context context, Callback callback) {
        super(context, callback);
    }

    @Override
    protected final Result run(File file, Params... params) throws Exception {
        return run(read(file), params);
    }

    private byte[] read(File file) throws IOException {
        AtomicFile atomicFile = new AtomicFile(file);
        return atomicFile.readFully();
    }

    protected abstract Result run(byte[] data, Params... params) throws Exception;

}
