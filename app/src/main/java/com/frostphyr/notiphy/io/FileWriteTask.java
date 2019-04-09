package com.frostphyr.notiphy.io;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.core.util.AtomicFile;

public abstract class FileWriteTask<Params, Result> extends FileIOTask<Params, Result> {

    public FileWriteTask(Context context, Callback callback) {
        super(context, callback);
    }

    @Override
    protected final Result run(File file, Params... params) throws Exception {
        write(file, getBytes(params));
        return null;
    }

    private void write(File file, byte[] data) throws IOException {
        AtomicFile atomicFile = new AtomicFile(file);
        FileOutputStream out = atomicFile.startWrite();
        out.write(data);
        atomicFile.finishWrite(out);
    }

    protected abstract byte[] getBytes(Params... params) throws Exception;

}
