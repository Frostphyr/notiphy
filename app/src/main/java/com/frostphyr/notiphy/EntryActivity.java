package com.frostphyr.notiphy;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class EntryActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 77;
    public static final int MAX_PHRASES = 10;

    public static final String EXTRA_ENTRY = "entry";

    protected Entry oldEntry;
    private Set<TextView> phraseViews = new LinkedHashSet<TextView>();

    protected abstract Entry createEntry();

    protected void init() {
        oldEntry = getIntent().getParcelableExtra(EXTRA_ENTRY);

        setSupportActionBar((Toolbar) findViewById(R.id.add_toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        actionBar.setDisplayHomeAsUpEnabled(true);

        TitledSpinner mediaSpinner = findViewById(R.id.media_spinner);
        if (mediaSpinner != null) {
            mediaSpinner.setAdapter(new ArrayAdapter<MediaType>(this, android.R.layout.simple_spinner_item, MediaType.values()));
        }

        TextView phrase1View = findViewById(R.id.phrase_1);
        if ((phrase1View) != null) {
            phraseViews.add(phrase1View);
        }

        View addNewPhraseButton = findViewById(R.id.add_new_phrase);
        if (addNewPhraseButton != null) {
            addNewPhraseButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    addNewPhrase(null);
                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_entry_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            case R.id.action_save:
                Entry entry = createEntry();
                if (entry != null) {
                    NotiphyApplication app = (NotiphyApplication) getApplication();
                    if (oldEntry != null) {
                        app.replaceEntry(oldEntry, entry);
                    } else {
                        app.getEntries().add(entry);
                    }
                    setResult(RESULT_OK);
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected MediaType getMediaType() {
        TitledSpinner mediaSpinner = findViewById(R.id.media_spinner);
        return mediaSpinner != null ? MediaType.valueOf(mediaSpinner.getSelectedItem()) : null;
    }

    protected String[] getPhrases() {
        List<String> phrases = new ArrayList<String>();
        for (TextView v : phraseViews) {
            String phrase = v.getText().toString().trim();
            if (!phrase.equals("")) {
                phrases.add(phrase);
            }
        }
        return phrases.toArray(new String[phrases.size()]);
    }

    protected void addNewPhrase(String text) {
        final ViewGroup layout = findViewById(R.id.add_entry_layout);
        final ViewGroup newLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.layout_add_entry_phrase, null, false);

        if (text != null) {
            EditText phraseView = newLayout.findViewById(R.id.phrase);
            phraseView.setText(text);
        }

        final View addNewPhraseButton = layout.findViewById(R.id.add_new_phrase);
        ImageButton removeButton = (ImageButton) newLayout.getChildAt(newLayout.indexOfChild(newLayout.findViewById(R.id.remove_phrase)));
        removeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                layout.removeView(newLayout);
                phraseViews.remove(newLayout);
                if (addNewPhraseButton.getVisibility() == View.GONE) {
                    addNewPhraseButton.setVisibility(View.VISIBLE);
                }
            }

        });


        layout.addView(newLayout, layout.indexOfChild(addNewPhraseButton));
        phraseViews.add((TextView) newLayout.findViewById(R.id.phrase));
        if (phraseViews.size() >= MAX_PHRASES) {
            addNewPhraseButton.setVisibility(View.GONE);
        }
    }

    protected void setMediaType(MediaType mediaType) {
        TitledSpinner mediaSpinner = findViewById(R.id.media_spinner);
        mediaSpinner.setSelectedItem(mediaType.ordinal());
    }

    protected void setPhrases(String[] phrases) {
        if (phrases.length >= 1) {
            EditText phrase1View = findViewById(R.id.phrase_1);
            phrase1View.setText(phrases[0]);

            for (int i = 1; i < phrases.length; i++) {
                addNewPhrase(phrases[i]);
            }
        }
    }

}
