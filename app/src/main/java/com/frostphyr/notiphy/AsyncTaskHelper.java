package com.frostphyr.notiphy;

import android.os.AsyncTask;

public abstract class AsyncTaskHelper<Params, Progress, Result> extends AsyncTask<Params, Progress, AsyncTaskResult<Result>> {

    private Callback<Result> callback;

    public AsyncTaskHelper(Callback<Result> callback) {
        this.callback = callback;
    }

    @Override
    protected AsyncTaskResult<Result> doInBackground(Params... params) {
        try {
            return new AsyncTaskResult<>(run(params));
        } catch (Exception e) {
            return new AsyncTaskResult<>(e);
        }
    }

    @Override
    protected void onPostExecute(AsyncTaskResult<Result> result) {
        if (result.getException() != null) {
            callback.onException(result.getException());
        } else {
            callback.onSuccess(result.getResult());
        }
    }

    protected abstract Result run(Params... params) throws Exception;

    public interface Callback<Result> {

        void onSuccess(Result result);

        void onException(Exception exception);

    }

}
