package com.frostphyr.notiphy;

import android.os.Bundle;
import android.widget.EditText;

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
        if (username.length() <= 0) {
            usernameView.setError("Username required");
        } else if (username.length() > 15) {
            usernameView.setError("Username cannot be longer than 15 characters");
        } else if (!username.matches("^[a-zA-Z0-9_]*$")) {
            usernameView.setError("Username can only contain alphanumeric characters and underscores");
        } else {
            return new TwitterEntry(username, getMediaType(), getPhrases());
        }
        return null;
    }

}
