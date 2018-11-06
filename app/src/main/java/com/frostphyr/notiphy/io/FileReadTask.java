package com.frostphyr.notiphy.io;

import android.content.Context;
import android.support.v4.util.AtomicFile;

import java.io.File;
import java.io.IOException;

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
