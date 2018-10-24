package com.frostphyr.notiphy.io;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.frostphyr.notiphy.AsyncTaskResult;

import java.io.IOException;
import java.net.URL;

public class ImageDownloader {

    public static void execute(String url, Callback callback) {
        new ImageDownloadTask(url, callback).execute();
    }

    public static interface Callback {

        void onDownload(Bitmap bitmap);

        void onFailure(Exception e);

    }

    private static class ImageDownloadTask extends AsyncTask<String, Void, AsyncTaskResult<Bitmap>> {

        private String url;
        private Callback callback;

        public ImageDownloadTask(String url, Callback callback) {
            this.url = url;
            this.callback = callback;
        }

        @Override
        protected AsyncTaskResult<Bitmap> doInBackground(String... strings) {
            try {
                return new AsyncTaskResult<Bitmap>(BitmapFactory.decodeStream(new URL(url).openStream()));
            } catch (IOException e) {
                return new AsyncTaskResult<Bitmap>(e);
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<Bitmap> result) {
            if (result.getResult() != null) {
                callback.onDownload(result.getResult());
            } else {
                callback.onFailure(result.getException());
            }
        }

    }

}
