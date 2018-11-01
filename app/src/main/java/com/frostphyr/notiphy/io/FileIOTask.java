package com.frostphyr.notiphy.io;

import android.content.Context;
import android.os.Environment;

import com.frostphyr.notiphy.AsyncTaskHelper;

import java.io.File;

public abstract class FileIOTask<Params, Result> extends AsyncTaskHelper<Params, Void, Result> {

    private Context context;

    public FileIOTask(Context context, Callback callback) {
        super(callback);

        this.context = context;
    }

    @Override
    protected Result run(Params... params) throws Exception {
        return run(getFile(), params);
    }

    private File getFile() {
        File dir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dir = context.getExternalFilesDir(null);
        } else {
            dir =  context.getFilesDir();
        }
        return new File(dir, getFileName());
    }

    protected abstract String getFileName();

    protected abstract Result run(File file, Params... params) throws Exception;

}
