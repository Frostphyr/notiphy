package com.frostphyr.notiphy.io;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.frostphyr.notiphy.Callback;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

public class UrlValidator {

    public static void validate(String url, Callback<Boolean> callback) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            callback.onComplete(new Callback.Result<>(false));
        } else {
            Http.getInstance().getClient().newCall(new Request.Builder()
                    .url(httpUrl)
                    .head()
                    .build())
                    .enqueue(new okhttp3.Callback() {

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                            int code = response.code();
                            if (code == 200) {
                                callback(new Callback.Result<>(true));
                            } else if (code == 404) {
                                callback(new Callback.Result<>(false));
                            } else {
                                callback(new Callback.Result<>(new IOException("HTTP response: " + code)));
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            callback(new Callback.Result<>(e));
                        }

                        private void callback(Callback.Result<Boolean> result) {
                            new Handler(Looper.getMainLooper()).post(() -> callback.onComplete(result));
                        }

                    });
        }
    }

}
