package com.frostphyr.notiphy.twitter;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.frostphyr.notiphy.CharRangeInputFilter;
import com.frostphyr.notiphy.EntryActivity;
import com.frostphyr.notiphy.MediaType;
import com.frostphyr.notiphy.NotiphyApplication;
import com.frostphyr.notiphy.R;

import org.json.JSONException;
import org.json.JSONObject;

public class TwitterActivity extends EntryActivity {

    private static final String FETCH_ID_REQUEST_TAG = "FetchId";

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

        ((NotiphyApplication) getApplication()).getRequestQueue().cancelAll(FETCH_ID_REQUEST_TAG);
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

        final View loadingView = findViewById(R.id.loading);
        loadingView.setVisibility(View.VISIBLE);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "http://frostphyr.com/notiphy/lookup_twitter_user_id.php?username=" + username, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        loadingView.setVisibility(View.GONE);
                        try {
                            if (response.has("error")) {
                                usernameView.setError(response.getString("error"));
                            } else {
                                finish(new TwitterEntry(response.getLong("id"), username, mediaType, phrases, oldEntry != null ? oldEntry.isActive() : true));
                            }
                        } catch (JSONException e) {
                            Toast.makeText(TwitterActivity.this, "Error communicating with Notiphy server", Toast.LENGTH_LONG);
                        }
                    }

                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingView.setVisibility(View.GONE);
                        Toast.makeText(TwitterActivity.this, "Error connecting to Notiphy server", Toast.LENGTH_LONG);
                    }

                });
        request.setTag(FETCH_ID_REQUEST_TAG);
        ((NotiphyApplication) getApplication()).getRequestQueue().add(request);
    }

}
