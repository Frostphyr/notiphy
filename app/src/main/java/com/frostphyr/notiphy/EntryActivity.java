package com.frostphyr.notiphy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.frostphyr.notiphy.io.Database;
import com.google.android.gms.ads.AdView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class EntryActivity extends AppCompatActivity {

    public static final String TAG = "EntryActivity";
    public static final String EXTRA_ENTRY = "com.frostphyr.notiphy.extra.ENTRY";
    public static final String EXTRA_EDIT_ENTRY = "com.frostphyr.notiphy.extra.EDIT_ENTRY";
    public static final int MAX_PHRASES = 10;

    protected DatabaseEntry oldEntry;
    private final Set<TextView> phraseViews = new LinkedHashSet<>();

    protected abstract void save();

    protected void init() {
        oldEntry = getIntent().getParcelableExtra(EXTRA_EDIT_ENTRY);

        AdView adView = findViewById(R.id.ad_banner);
        adView.loadAd(AndroidUtils.generateAdRequest());

        TextInputLayout phrase1View = findViewById(R.id.phrase_1);
        if ((phrase1View) != null) {
            phraseViews.add(phrase1View.getEditText());
        }

        View addNewPhraseButton = findViewById(R.id.add_new_phrase);
        if (addNewPhraseButton != null) {
            addNewPhraseButton.setOnClickListener(view -> addNewPhrase(null));
        }

        MaterialToolbar toolbar = findViewById(R.id.entry_toolbar);
        if (oldEntry == null) {
            toolbar.getMenu().removeItem(R.id.action_delete);
        }
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) {
                save();
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                showDeleteConfirmation();
                return true;
            }
            return false;
        });

        findViewById(R.id.phrases_description).setOnClickListener(view ->
                AndroidUtils.showDialog(EntryActivity.this, R.string.phrases, R.string.description_phrases));
    }

    protected void finish(Entry entry) {
        asyncStart();
        if (oldEntry != null) {
            if (entry == null) {
                Database.deleteEntry(oldEntry, this::handleResult);
            } else if (entry.equals(oldEntry.getEntry())) {
                setResult(RESULT_CANCELED);
                finish();
            } else {
                Database.replaceEntry(oldEntry, entry, this::handleResult);
            }
        } else if (entry != null) {
            Database.addEntry(entry, this::handleResult);
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    protected void asyncStart() {
        findViewById(R.id.entry_content).setVisibility(View.GONE);
        findViewById(R.id.entry_progress).setVisibility(View.VISIBLE);
    }

    protected void asyncStop() {
        findViewById(R.id.entry_progress).setVisibility(View.GONE);
        findViewById(R.id.entry_content).setVisibility(View.VISIBLE);
    }

    private void handleResult(Callback.Result<?> result) {
        Exception e = result.getException();
        if (e == null) {
            Intent intent = new Intent();
            if (result.getData() != null) {
                intent.putExtra(EXTRA_ENTRY, (Parcelable) result.getData());
            }
            if (oldEntry != null) {
                intent.putExtra(EXTRA_EDIT_ENTRY, oldEntry);
            }
            setResult(RESULT_OK, intent);
            finish();
        } else if (e instanceof UserNotSignedInException) {
            startActivity(new Intent(this, AuthActivity.class)
                    .putExtra(AuthActivity.EXTRA_RETURN_TO_CALLER, true));
            asyncStop();
        } else {
            AndroidUtils.handleError(this, e, R.string.error_message_creating_entry);
            asyncStop();
        }
    }

    protected List<String> getPhrases() {
        List<String> phrases = new ArrayList<>();
        for (TextView v : phraseViews) {
            String phrase = v.getText().toString().trim();
            if (!phrase.equals("")) {
                phrases.add(phrase);
            }
        }
        return phrases;
    }

    protected void addNewPhrase(String text) {
        final ViewGroup layout = findViewById(R.id.entry_phrases_layout);
        final ViewGroup newLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.layout_entry_phrase, layout, false);
        final TextInputLayout phraseView = newLayout.findViewById(R.id.phrase);
        if (text != null) {
            phraseView.getEditText().setText(text);
        }

        final View addNewPhraseButton = layout.findViewById(R.id.add_new_phrase);
        ImageButton removeButton = newLayout.findViewById(R.id.remove_phrase);
        removeButton.setOnClickListener(view -> {
            layout.removeView(newLayout);
            phraseViews.remove(phraseView.getEditText());
            if (addNewPhraseButton.getVisibility() == View.GONE) {
                addNewPhraseButton.setVisibility(View.VISIBLE);
            }
        });

        layout.addView(newLayout, layout.indexOfChild(addNewPhraseButton));
        phraseViews.add(phraseView.getEditText());
        if (phraseViews.size() >= MAX_PHRASES) {
            addNewPhraseButton.setVisibility(View.GONE);
        }
    }

    protected void setPhrases(List<String> phrases) {
        if (phrases.size() >= 1) {
            TextInputLayout phrase1View = findViewById(R.id.phrase_1);
            phrase1View.getEditText().setText(phrases.get(0));

            for (Iterator<String> it = phrases.listIterator(1); it.hasNext(); ) {
                addNewPhrase(it.next());
            }
        }
    }

    protected void showMessage(int messageResId) {
        Toast.makeText(this, messageResId, Toast.LENGTH_LONG).show();
    }

    private void showDeleteConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.confirmation_delete_entry)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    dialog.dismiss();
                    finish(null);
                })
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static class NewContract extends ActivityResultContract<EntryType, DatabaseEntry> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, EntryType type) {
            return new Intent(context, type.getActivityClass());
        }

        @Override
        public DatabaseEntry parseResult(int resultCode, @Nullable Intent intent) {
            return resultCode == RESULT_OK && intent != null ? intent.getParcelableExtra(EXTRA_ENTRY) : null;
        }

    }

    public static class EditContract extends ActivityResultContract<DatabaseEntry, Bundle> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, DatabaseEntry entry) {
            Intent intent = new Intent(context, entry.getEntry().getType().getActivityClass());
            intent.putExtra(EXTRA_EDIT_ENTRY, entry);
            return intent;
        }

        @Override
        public Bundle parseResult(int resultCode, @Nullable Intent intent) {
            return resultCode == RESULT_OK && intent != null ? intent.getExtras() : null;
        }

    }

}
