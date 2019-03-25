package com.frostphyr.notiphy.twitter;

import com.frostphyr.notiphy.io.JsonHttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;

public class TwitterUserIdRequest {

    public static final int ERROR_CONNECTION = -1;
    public static final int ERROR_USER_NOT_FOUND = 50;
    public static final int ERROR_USER_SUSPENDED = 63;

    private JsonHttpRequest request;

    public TwitterUserIdRequest(OkHttpClient client, String username, final Callback callback) {
        request = new JsonHttpRequest(client, "http://frostphyr.com/scripts/lookup_twitter_user_id.php", new JsonHttpRequest.Callback() {

            @Override
            public void onResult(JSONObject obj) {
                if (obj != null) {
                    try {
                        if (obj.has("id")) {
                            callback.onResult(obj.getString("id"));
                        } else {
                            callback.onError(obj.getInt("error_code"));
                        }
                    } catch (JSONException e) {
                        callback.onError(-1);
                    }
                } else {
                    callback.onError(-1);
                }
            }

        }).addParameter("username", username).send();
    }

    public void cancel() {
        request.cancel();
    }

    public interface Callback {

        void onResult(String userId);

        void onError(int code);

    }

}
