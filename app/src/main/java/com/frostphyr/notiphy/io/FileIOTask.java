package com.frostphyr.notiphy.io;

import android.content.Context;

import com.frostphyr.notiphy.AsyncTaskHelper;

import java.io.File;

public abstract class FileIOTask<Params, Result> extends AsyncTaskHelper<Params, Void, Result> {

    private Context context;

    public FileIOTask(Context context, Callback<Result> callback) {
        super(callback);

        this.context = context;
    }

    @Override
    protected Result run(Params... params) throws Exception {
        return run(new File(context.getFilesDir(), getFileName()), params);
    }

    protected abstract String getFileName();

    protected abstract Result run(File file, Params... params) throws Exception;

}
