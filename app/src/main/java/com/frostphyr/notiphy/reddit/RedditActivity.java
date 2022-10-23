package com.frostphyr.notiphy.reddit;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannedString;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.frostphyr.notiphy.AndroidUtils;
import com.frostphyr.notiphy.CharRangeInputFilter;
import com.frostphyr.notiphy.EntryActivity;
import com.frostphyr.notiphy.R;
import com.frostphyr.notiphy.io.UrlValidator;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;

public class RedditActivity extends EntryActivity {

    private static final String REDDIT_URL = "https://www.reddit.com/";
    private static final String[] BLOCKED_SUBREDDITS = {
            "all",
            "popular",
            "frontpage"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reddit);

        init();
    }

    @Override
    protected void init() {
        super.init();

        TextInputLayout valueView = findViewById(R.id.reddit_value);
        valueView.getEditText().addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                findViewById(R.id.action_save).setEnabled(s.length() >= 3);
                MaterialToolbar toolbar = findViewById(R.id.entry_toolbar);
                Drawable saveIcon = toolbar.getMenu().findItem(R.id.action_save).getIcon().mutate();
                if (s.length() >= 3) {
                    saveIcon.clearColorFilter();
                } else {
                    saveIcon.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });

        Spinner typeSpinner = findViewById(R.id.reddit_type_spinner);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                RedditEntryType entryType = (RedditEntryType) parent.getItemAtPosition(position);
                TextInputLayout valueView = findViewById(R.id.reddit_value);
                valueView.setHint(entryType.getIconResource().getStringResId());

                EditText valueText = valueView.getEditText();
                InputFilter[] filters = new InputFilter[] {
                        new InputFilter.LengthFilter(entryType.getCharLimit()),
                        new CharRangeInputFilter(entryType.getCharRanges())
                };
                valueText.setFilters(filters);

                CharSequence text = valueText.getText();
                for (InputFilter filter : filters) {
                    CharSequence filteredText = filter.filter(text, 0, text.length(), new SpannedString(""), 0, 0);
                    if (filteredText != null) {
                        text = filteredText;
                    }
                }
                valueText.setText(text);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }

        });
        typeSpinner.setAdapter(AndroidUtils.createSpinnerAdapter(this, RedditEntryType.values()));

        Spinner postTypeSpinner = findViewById(R.id.reddit_post_type_spinner);
        postTypeSpinner.setAdapter(AndroidUtils.createSpinnerAdapter(this, RedditPostType.values()));

        if (oldEntry != null) {
            RedditEntry entry = oldEntry.getEntry();
            valueView.getEditText().setText(entry.getValue());
            typeSpinner.setSelection(entry.getEntryType().ordinal());
            postTypeSpinner.setSelection(entry.getPostType().ordinal());
            setPhrases(entry.getPhrases());
        }
    }

    @Override
    protected void save() {
        Spinner postTypeSpinner = findViewById(R.id.reddit_post_type_spinner);
        RedditPostType postType = (RedditPostType) postTypeSpinner.getSelectedItem();

        Spinner typeSpinner = findViewById(R.id.reddit_type_spinner);
        RedditEntryType entryType = (RedditEntryType) typeSpinner.getSelectedItem();

        TextInputLayout valueView = findViewById(R.id.reddit_value);
        String value = valueView.getEditText().getText().toString();

        if (entryType == RedditEntryType.SUBREDDIT && isSubredditBlocked(value)) {
            showMessage(R.string.error_message_reddit_blocked_subreddit);
            return;
        }

        if (oldEntry != null) {
            RedditEntry entry = oldEntry.getEntry();
            if (entry.getEntryType() == entryType && entry.getValue().equalsIgnoreCase(value)) {
                finish(new RedditEntry(entryType, value, postType, getPhrases(), oldEntry.getEntry().isActive()));
                return;
            }
        }

        asyncStart();
        UrlValidator.validate(REDDIT_URL + entryType.getPrefix() + value, result -> {
            if (Boolean.TRUE.equals(result.getData())) {
                finish(new RedditEntry(entryType, value, postType, getPhrases(),
                        oldEntry == null || oldEntry.getEntry().isActive()));
            } else if (Boolean.FALSE.equals(result.getData())) {
                valueView.setError(getString(R.string.error_message_not_found_arg, getString(entryType.getIconResource().getStringResId())));
                asyncStop();
            } else {
                AndroidUtils.handleError(this, result.getException(), R.string.error_message_creating_entry);
                asyncStop();
            }
        });
    }

    private boolean isSubredditBlocked(String value) {
        for (String s : BLOCKED_SUBREDDITS) {
            if (s.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

}
