package com.frostphyr.notiphy;

import android.os.Bundle;
import android.widget.TextView;

public class AddTwitterActivity extends AddEntryActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_twitter);

        init();
    }

    @Override
    protected Entry createEntry() {
        TextView usernameView = findViewById(R.id.username);
        return new TwitterEntry(usernameView.getText().toString(), getMediaType(), getPhrases());
    }

}
