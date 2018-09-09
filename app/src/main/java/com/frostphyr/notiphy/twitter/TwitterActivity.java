package com.frostphyr.notiphy.twitter;

import android.os.Bundle;
import android.text.InputFilter;
import android.widget.EditText;

import com.frostphyr.notiphy.CharRangeInputFilter;
import com.frostphyr.notiphy.Entry;
import com.frostphyr.notiphy.EntryActivity;
import com.frostphyr.notiphy.MediaType;
import com.frostphyr.notiphy.R;

public class TwitterActivity extends EntryActivity {

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
    protected Entry createEntry() {
        EditText usernameView = findViewById(R.id.username);
        String username = usernameView.getText().toString().trim();
        MediaType mediaType = getMediaType();
        String[] phrases = getPhrases();
        return new TwitterEntry(username, mediaType, phrases, oldEntry != null ? oldEntry.isActive() : true);
    }

}
