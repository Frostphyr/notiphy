package com.frostphyr.notiphy;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Switch;

public class AddTwitterActivity extends AddEntryActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_twitter);

        init();
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
            return new TwitterEntry(username, mediaType, phrases, true);
        }
        return null;
    }

}
