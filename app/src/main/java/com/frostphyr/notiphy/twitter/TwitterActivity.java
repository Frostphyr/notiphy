package com.frostphyr.notiphy.twitter;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.Spinner;

import com.frostphyr.notiphy.AndroidUtils;
import com.frostphyr.notiphy.CharRangeInputFilter;
import com.frostphyr.notiphy.EntryActivity;
import com.frostphyr.notiphy.MediaType;
import com.frostphyr.notiphy.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;

public class TwitterActivity extends EntryActivity {

    private static final char[][] USERNAME_CHAR_RANGES = {
            {'a', 'z'},
            {'A', 'Z'},
            {'0', '9'},
            {'_'}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter);

        init();
    }

    @Override
    protected void init() {
        super.init();

        TextInputLayout usernameView = findViewById(R.id.twitter_username);
        usernameView.getEditText().setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(15),
                new CharRangeInputFilter(USERNAME_CHAR_RANGES)
        });
        usernameView.getEditText().addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSaveState(s);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });

        Spinner mediaSpinner = findViewById(R.id.media_spinner);
        if (mediaSpinner != null) {
            mediaSpinner.setAdapter(AndroidUtils.createSpinnerAdapter(this, MediaType.values()));
        }

        if (oldEntry != null) {
            TwitterEntry entry = oldEntry.getEntry();
            usernameView.getEditText().setText(entry.getUsername());
            setMediaType(entry.getMediaType());
            setPhrases(entry.getPhrases());
        } else {
            updateSaveState("");
        }
    }

    private void updateSaveState(CharSequence username) {
        findViewById(R.id.action_save).setEnabled(username.length() >= 1);
        MaterialToolbar toolbar = findViewById(R.id.entry_toolbar);
        Drawable saveIcon = toolbar.getMenu().findItem(R.id.action_save).getIcon().mutate();
        if (username.length() >= 1) {
            saveIcon.clearColorFilter();
        } else {
            saveIcon.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    protected void save() {
        asyncStart();
        TextInputLayout usernameView = findViewById(R.id.twitter_username);
        String username = usernameView.getEditText().getText().toString();
        TwitterEntry entry = oldEntry != null ? oldEntry.getEntry() : null;
        if (oldEntry != null && entry.getUsername().equalsIgnoreCase(username)) {
            finish(entry.getUserId(), username);
        } else {
            TwitterUserIdLookup.lookup(this, username, result -> {
                if (result.getData() != null) {
                    finish(result.getData(), username);
                } else if (result.getException() instanceof TwitterApiException) {
                    int code = ((TwitterApiException) result.getException()).getCode();
                    if (code == TwitterApiException.CODE_USER_NOT_FOUND) {
                        usernameView.setError(getString(R.string.error_message_user_not_found));
                    } else if (code == TwitterApiException.CODE_USER_SUSPENDED) {
                        usernameView.setError(getString(R.string.error_message_user_suspended));
                    } else {
                        AndroidUtils.handleError(this, result.getException(), R.string.error_message_creating_entry);
                    }
                    asyncStop();
                } else {
                    AndroidUtils.handleError(this, result.getException(), R.string.error_message_creating_entry);
                    asyncStop();
                }
            });
        }
    }

    protected MediaType getMediaType() {
        Spinner mediaSpinner = findViewById(R.id.media_spinner);
        return mediaSpinner != null ? (MediaType) mediaSpinner.getSelectedItem() : null;
    }

    protected void setMediaType(MediaType mediaType) {
        Spinner mediaSpinner = findViewById(R.id.media_spinner);
        mediaSpinner.setSelection(mediaType.ordinal());
    }

    private void finish(String userId, String username) {
        finish(new TwitterEntry(userId, username, getMediaType(), getPhrases(),
                oldEntry == null || oldEntry.getEntry().isActive()));
    }

}
