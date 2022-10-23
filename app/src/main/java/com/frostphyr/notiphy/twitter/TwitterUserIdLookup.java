package com.frostphyr.notiphy.twitter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.frostphyr.notiphy.Callback;
import com.frostphyr.notiphy.R;
import com.frostphyr.notiphy.io.Http;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class TwitterUserIdLookup {

    public static void lookup(Context context, String username, Callback<String> callback) {
        HttpUrl url = HttpUrl.parse(context.getString(R.string.twitter_user_id_lookup_url));
        if (url == null) {
            callback.onComplete(new Callback.Result<>(new MalformedURLException()));
        } else {
            Http.getInstance().getClient().newCall(new Request.Builder()
                    .url(url.newBuilder().addQueryParameter("username", username).build())
                    .build())
                    .enqueue(new okhttp3.Callback() {

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                            try (ResponseBody body = response.body()) {
                                JSONObject obj = new JSONObject(body.string());
                                if (obj.has("id")) {
                                    callback(new Callback.Result<>(obj.getString("id")));
                                } else if (obj.has("error_code")) {
                                    callback(new Callback.Result<>(new TwitterApiException(obj.getInt("error_code"))));
                                } else {
                                    callback(new Callback.Result<>(new IOException()));
                                }
                            } catch (JSONException | IOException e) {
                                callback(new Callback.Result<>(e));
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            callback(new Callback.Result<>(e));
                        }

                        private void callback(Callback.Result<String> result) {
                            new Handler(Looper.getMainLooper()).post(() -> callback.onComplete(result));
                        }

                    });
        }
    }

}
