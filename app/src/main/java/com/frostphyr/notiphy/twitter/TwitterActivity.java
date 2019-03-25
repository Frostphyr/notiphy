package com.frostphyr.notiphy.twitter;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.frostphyr.notiphy.CharRangeInputFilter;
import com.frostphyr.notiphy.EntryActivity;
import com.frostphyr.notiphy.NotiphyApplication;
import com.frostphyr.notiphy.R;

public class TwitterActivity extends EntryActivity {

    private TwitterUserIdRequest idRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter);

        init();
    }

    @Override
    protected void init() {
        super.init();

        EditText usernameView = findViewById(R.id.twitter_username);
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

        if (idRequest != null) {
            idRequest.cancel();
        }
    }

    @Override
    protected void save() {
        EditText usernameView = findViewById(R.id.twitter_username);
        String username = usernameView.getText().toString().trim();
        if (username.length() == 0) {
            usernameView.setError("Please enter a username");
        }

        View loadingView = findViewById(R.id.twitter_loading);
        loadingView.setVisibility(View.VISIBLE);
        fetchUserId(usernameView, loadingView, username);
    }

    private void fetchUserId(final EditText usernameView, final View loadingView, final String username) {
        idRequest = new TwitterUserIdRequest(((NotiphyApplication) getApplication()).getHttpClient(), username, new TwitterUserIdRequest.Callback() {

            @Override
            public void onResult(final String userId) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        finish(new TwitterEntry(userId, username, getMediaType(), getPhrases(), oldEntry == null || oldEntry.isActive()));
                    }

                });
            }

            @Override
            public void onError(final int code) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        loadingView.setVisibility(View.GONE);
                        switch (code) {
                            case TwitterUserIdRequest.ERROR_CONNECTION:
                                Toast.makeText(TwitterActivity.this, R.string.error_message_twitter_connection, Toast.LENGTH_LONG).show();
                                break;
                            case TwitterUserIdRequest.ERROR_USER_NOT_FOUND:
                                usernameView.setError(getString(R.string.error_message_user_not_found));
                                break;
                            case TwitterUserIdRequest.ERROR_USER_SUSPENDED:
                                usernameView.setError(getString(R.string.error_message_user_suspended));
                            default:
                                Toast.makeText(TwitterActivity.this, R.string.error_message_fetch_user_id, Toast.LENGTH_LONG).show();
                                break;

                        }
                    }

                });
            }

        });
    }

}
