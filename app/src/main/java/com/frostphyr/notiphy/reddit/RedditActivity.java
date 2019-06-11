package com.frostphyr.notiphy.reddit;

import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;

import com.frostphyr.notiphy.BasicSpinnerAdapter;
import com.frostphyr.notiphy.BasicSpinnerIconAdapter;
import com.frostphyr.notiphy.CharRangeInputFilter;
import com.frostphyr.notiphy.EntryActivity;
import com.frostphyr.notiphy.IllegalInputException;
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
        valueView.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(21),
                new CharRangeInputFilter(RedditEntry.VALUE_CHAR_RANGES)
        });

        Spinner typeSpinner = findViewById(R.id.reddit_type_spinner);
        typeSpinner.setAdapter(new BasicSpinnerAdapter<>(this, RedditEntryType.values()));

        TitledSpinner postTypeSpinner = findViewById(R.id.reddit_post_type_spinner);
        postTypeSpinner.setAdapter(new BasicSpinnerIconAdapter<>(this, RedditPostType.values()));

        RedditEntry entry = (RedditEntry) oldEntry;
        if (entry != null) {
            valueView.setText(entry.getValue());
            typeSpinner.setSelection(entry.getEntryType().ordinal());
            postTypeSpinner.setSelectedItem(entry.getPostType().ordinal());
            setPhrases(entry.getPhrases());
        }
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
        } catch (IllegalInputException e) {
            valueView.setError(getResources().getString(e.getErrorMessageResourceId()));
        }
    }

}
