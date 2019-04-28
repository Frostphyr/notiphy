package com.frostphyr.notiphy.reddit;

import android.os.Bundle;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.Spinner;

import com.frostphyr.notiphy.BasicSpinnerAdapter;
import com.frostphyr.notiphy.BasicSpinnerIconAdapter;
import com.frostphyr.notiphy.CharRangeInputFilter;
import com.frostphyr.notiphy.EntryActivity;
import com.frostphyr.notiphy.R;
import com.frostphyr.notiphy.TitledSpinner;

public class RedditActivity extends EntryActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reddit);

        init();
    }

    @Override
    protected void init() {
        super.init();

        EditText valueView = findViewById(R.id.reddit_value);
        valueView.setFilters(new InputFilter[]{new CharRangeInputFilter(RedditEntry.VALUE_CHAR_RANGES)});

        ((Spinner) findViewById(R.id.reddit_type_spinner))
                .setAdapter(new BasicSpinnerAdapter<>(this, RedditEntryType.values()));
        ((TitledSpinner) findViewById(R.id.reddit_post_type_spinner))
                .setAdapter(new BasicSpinnerIconAdapter<>(this, RedditPostType.values()));
    }

    @Override
    protected void save() {
        Spinner typeSpinner = findViewById(R.id.reddit_type_spinner);
        RedditEntryType type = (RedditEntryType) typeSpinner.getSelectedItem();

        EditText valueView = findViewById(R.id.reddit_value);
        String value = valueView.getText().toString().trim();

        TitledSpinner postTypeSpinner = findViewById(R.id.reddit_post_type_spinner);
        RedditPostType postType = (RedditPostType) postTypeSpinner.getSelectedItem();

        try {
            finish(new RedditEntry(type, value, postType, getPhrases(), true));
        } catch (IllegalArgumentException e) {
            valueView.setError(e.getMessage());
        }
    }

}
