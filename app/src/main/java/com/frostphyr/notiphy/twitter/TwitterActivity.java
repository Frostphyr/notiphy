package com.frostphyr.notiphy.twitter;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.frostphyr.notiphy.CharRangeInputFilter;
import com.frostphyr.notiphy.EntryActivity;
import com.frostphyr.notiphy.MediaType;
import com.frostphyr.notiphy.NotiphyApplication;
import com.frostphyr.notiphy.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

public class TwitterActivity extends EntryActivity {

    private Call userIdCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter);

        init();

        EditText usernameView = findViewById(R.id.username);
        usernameView.setFilters(new InputFilter[]{new CharRangeInputFilter(TwitterEntry.USERNAME_CHAR_RANGES)});

        TwitterEntry oldEntry = (TwitterEntry) super.oldEntry;
        if (oldEntry != null) {
            usernameView.setText(oldEntry.getUsername());
            setMediaType(oldEntry.getMediaType());
            setPhrases(oldEntry.getPhrases());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (userIdCall != null) {
            userIdCall.cancel();
        }
    }

    @Override
    protected void createEntry() {
        final EditText usernameView = findViewById(R.id.username);
        final String username = usernameView.getText().toString().trim();
        if (username.length() == 0) {
            usernameView.setError("Please enter a username");
            return;
        }

        final MediaType mediaType = getMediaType();
        final String[] phrases = getPhrases();

        findViewById(R.id.loading).setVisibility(View.VISIBLE);

        HttpUrl url = HttpUrl.parse("http://frostphyr.com/notiphy/lookup_twitter_user_id.php").newBuilder()
                .addQueryParameter("username", username)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        userIdCall = ((NotiphyApplication) getApplication()).getHttpClient().newCall(request);
        userIdCall.enqueue(new Callback() {

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        onUserIdResponse(response, username, mediaType, phrases);
                    }

                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        onUserIdError();
                    }

                });
            }

        });
    }

    private void onUserIdResponse(Response response, String username, MediaType mediaType, String[] phrases) {
        findViewById(R.id.loading).setVisibility(View.GONE);
        try {
            JSONObject obj = new JSONObject(response.body().string());
            if (obj.has("error")) {
                EditText usernameView = findViewById(R.id.username);
                usernameView.setError(obj.getString("error"));
            } else {
                finish(new TwitterEntry(obj.getString("id"), username, mediaType, phrases, oldEntry != null ? oldEntry.isActive() : true));
            }
        } catch (JSONException | IOException e) {
            Toast.makeText(TwitterActivity.this, "Error communicating with Notiphy server", Toast.LENGTH_LONG);
        }
    }

    private void onUserIdError() {
        findViewById(R.id.loading).setVisibility(View.GONE);
        Toast.makeText(TwitterActivity.this, "Error connecting to Notiphy server", Toast.LENGTH_LONG);
    }

}
