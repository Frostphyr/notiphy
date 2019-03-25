package com.frostphyr.notiphy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
            ArrayAdapter<MediaType> adapter = new MediaTypeAdapter();
            adapter.setDropDownViewResource(R.layout.layout_spinner_dropdown_item);
            mediaSpinner.setAdapter(adapter);
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
                new AlertDialog.Builder(this)
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
        return mediaSpinner != null ? MediaType.valueOf(mediaSpinner.getSelectedItem()) : null;
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

    private class MediaTypeAdapter extends ArrayAdapter<MediaType> {

        private LayoutInflater inflater;

        public MediaTypeAdapter() {
            super(EntryActivity.this, -1, MediaType.values());

            inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createView(position, parent,
                    R.layout.layout_spinner_item, R.id.spinner_item_image, R.id.spinner_item_text);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return createView(position, parent,
                    R.layout.layout_spinner_dropdown_item, R.id.spinner_dropdown_item_image, R.id.spinner_dropdown_item_text);
        }

        private View createView(int position, ViewGroup parent,
                                int layoutResId, int imageResId, int textResId) {
            View view = inflater.inflate(layoutResId, parent, false);
            MediaType mediaType = getItem(position);
            ImageView imageView = view.findViewById(imageResId);
            if (mediaType.getIconResId() == -1) {
                imageView.setVisibility(View.GONE);
            } else {
                imageView.setImageResource(mediaType.getIconResId());
            }
            ((TextView) view.findViewById(textResId)).setText(mediaType.name());
            return view;
        }

    }

}
