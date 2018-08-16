package com.frostphyr.notiphy;

import android.os.Bundle;
import android.widget.EditText;

public class TwitterActivity extends EntryActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_twitter);

        init();

        TwitterEntry oldEntry = (TwitterEntry) super.oldEntry;
        if (oldEntry != null) {
            EditText usernameView = findViewById(R.id.username);
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
        String usernameError = TwitterEntry.validateUsername(username);
        if (usernameError != null) {
            usernameView.setError(usernameError);
        } else if (TwitterEntry.validateMediaType(mediaType) != null
                || TwitterEntry.validatePhrases(phrases) != null) {
            //Shouldn't happen
        } else {
            return new TwitterEntry(username, mediaType, phrases, oldEntry != null ? oldEntry.isActive() : true);
        }
        return null;
    }

}
