package com.frostphyr.notiphy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class EntryActivity extends AppCompatActivity {

    public static final int MAX_PHRASES = 10;

    public static final String EXTRA_ENTRY = "entry";
    public static final String EXTRA_OLD_ENTRY = "oldEntry";

    public static final int REQUEST_CODE_NEW = 100;
    public static final int REQUEST_CODE_EDIT = 101;

    protected Entry oldEntry;
    private Set<TextView> phraseViews = new LinkedHashSet<>();

    protected abstract void save();

    protected void init() {
        oldEntry = getIntent().getParcelableExtra(EXTRA_ENTRY);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        actionBar.setDisplayHomeAsUpEnabled(true);

        TitledSpinner mediaSpinner = findViewById(R.id.media_spinner);
        if (mediaSpinner != null) {
            mediaSpinner.setAdapter(new BasicSpinnerIconAdapter<>(this, MediaType.values()));
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
        inflater.inflate(R.menu.entry_toolbar_menu, menu);
        if (oldEntry == null) {
            menu.removeItem(R.id.action_delete);
        }
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
                save();
                return true;
            case R.id.action_delete:
                new AlertDialog.Builder(this, R.style.NotiphyTheme_AlertDialog)
                        .setMessage(R.string.delete_entry_conformation)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish(null);
                            }

                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }

                        })
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void finish(Entry entry) {
        Intent intent = new Intent();
        if (entry != null) {
            intent.putExtra(EXTRA_ENTRY, entry);
        }
        if (oldEntry != null) {
            intent.putExtra(EXTRA_OLD_ENTRY, oldEntry);
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    protected MediaType getMediaType() {
        TitledSpinner mediaSpinner = findViewById(R.id.media_spinner);
        return mediaSpinner != null ? (MediaType) mediaSpinner.getSelectedItem() : null;
    }

    protected String[] getPhrases() {
        List<String> phrases = new ArrayList<>();
        for (TextView v : phraseViews) {
            String phrase = v.getText().toString().trim();
            if (!phrase.equals("")) {
                phrases.add(phrase);
            }
        }
        return phrases.toArray(new String[0]);
    }

    protected void addNewPhrase(String text) {
        final ViewGroup layout = findViewById(R.id.entry_layout);
        final ViewGroup newLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.layout_entry_phrase, null, false);
        final EditText phraseView = newLayout.findViewById(R.id.phrase);

        if (text != null) {
            phraseView.setText(text);
        }

        final View addNewPhraseButton = layout.findViewById(R.id.add_new_phrase);
        ImageButton removeButton = (ImageButton) newLayout.getChildAt(newLayout.indexOfChild(newLayout.findViewById(R.id.remove_phrase)));
        removeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                layout.removeView(newLayout);
                phraseViews.remove(phraseView);
                if (addNewPhraseButton.getVisibility() == View.GONE) {
                    addNewPhraseButton.setVisibility(View.VISIBLE);
                }
            }

        });

        layout.addView(newLayout, layout.indexOfChild(addNewPhraseButton));
        phraseViews.add(phraseView);
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
