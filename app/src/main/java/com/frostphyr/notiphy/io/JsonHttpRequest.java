package com.frostphyr.notiphy.io;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JsonHttpRequest {

    private HttpUrl.Builder builder;
    private OkHttpClient client;
    private Callback callback;
    private Call call;

    public JsonHttpRequest(OkHttpClient client, String url, Callback callback) {
        this.client = client;
        this.callback = callback;

        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            throw new IllegalArgumentException();
        }
        builder = httpUrl.newBuilder();
    }

    public JsonHttpRequest addParameter(String name, String value) {
        builder.addQueryParameter(name, value);
        return this;
    }

    public JsonHttpRequest send() {
        Request request = new Request.Builder()
                .url(builder.build())
                .build();

        call = client.newCall(request);
        call.enqueue(new okhttp3.Callback() {

            @Override
            public void onResponse(Call call, final Response response) {
                try {
                    callback.onResult(new JSONObject(response.body().string()));
                } catch(JSONException | IOException e) {
                    callback.onResult(null);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onResult(null);
            }

        });
        return this;
    }

    public void cancel() {
        if (call != null) {
            call.cancel();
        }
    }

    public interface Callback {

        void onResult(JSONObject obj);

    }

}
