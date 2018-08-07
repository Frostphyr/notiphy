package com.frostphyr.notiphy;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class AddEntryActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 77;
    public static final int MAX_PHRASES = 10;

    private Set<TextView> phraseViews = new LinkedHashSet<TextView>();

    protected abstract Entry createEntry();

    protected void init() {
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
                    addNewPhrase(view);
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
                finish();
                return true;
            case R.id.action_save:
                Entry entry = createEntry();
                if (entry != null) {
                    Intent data = new Intent();
                    data.putExtra("entry", entry);
                    setResult(RESULT_OK, data);
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

    private void addNewPhrase(View v) {
        final ViewGroup layout = findViewById(R.id.add_entry_layout);
        final ViewGroup newLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.layout_add_entry_phrase, null, false);
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

}
