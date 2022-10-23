package com.frostphyr.notiphy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.frostphyr.notiphy.io.Database;
import com.frostphyr.notiphy.settings.SettingsActivity;
import com.google.android.gms.ads.AdView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class EntryListActivity extends AppCompatActivity {
    
    private static final int MAX_ENTRIES = 25;

    private List<DatabaseEntry> entries;
    private MenuPopupHelper addMenuHelper;
    private String uid;

    private final ActivityResultLauncher<EntryType> newEntryLauncher =
            registerForActivityResult(new EntryActivity.NewContract(), entry -> {
                if (entry != null) {
                    addEntry(entry);
                }
            });

    private final ActivityResultLauncher<DatabaseEntry> editEntryLauncher =
            registerForActivityResult(new EntryActivity.EditContract(), bundle -> {
                if (bundle != null) {
                    DatabaseEntry oldEntry = bundle.getParcelable(EntryActivity.EXTRA_EDIT_ENTRY);
                    DatabaseEntry newEntry = bundle.getParcelable(EntryActivity.EXTRA_ENTRY);
                    if (newEntry != null) {
                        replaceEntry(oldEntry, newEntry);
                    } else {
                        removeEntry(oldEntry);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_list);

        AdView adView = findViewById(R.id.ad_banner);
        adView.loadAd(AndroidUtils.generateAdRequest());

        RecyclerView entryList = findViewById(R.id.entry_list);
        entryList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        MaterialToolbar toolbar = findViewById(R.id.entry_list_toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_refresh) {
                refresh();
                return true;
            } else if (item.getItemId() == R.id.action_add) {
                showAddPopupMenu(item);
                return true;
            } else if (item.getItemId() == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });

        validateUser();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();

        validateUser();
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onPause() {
        super.onPause();

        if (addMenuHelper != null && addMenuHelper.isShowing()) {
            addMenuHelper.dismiss();
            addMenuHelper = null;
        }
    }

    private void asyncStart() {
        findViewById(R.id.entry_list_content).setVisibility(View.GONE);
        findViewById(R.id.entry_list_progress).setVisibility(View.VISIBLE);
    }

    private void asyncStop() {
        findViewById(R.id.entry_list_progress).setVisibility(View.GONE);
        findViewById(R.id.entry_list_content).setVisibility(View.VISIBLE);
    }

    private void validateUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (uid == null || !user.getUid().equals(uid)) {
                uid = user.getUid();
                fetchEntries();
            } else {
                asyncStop();
            }
        } else {
            reauthenticate();
        }
    }

    private void reauthenticate() {
        startActivity(new Intent(this, AuthActivity.class)
                .putExtra(AuthActivity.EXTRA_RETURN_TO_CALLER, true));
    }

    public void fetchEntries() {
        asyncStart();
        Database.getEntries(result -> {
            if (result.getData() != null) {
                entries = result.getData();
            } else if (result.getException() instanceof UserNotSignedInException) {
                reauthenticate();
            } else {
                entries = new ArrayList<>();
                AndroidUtils.handleError(this, result.getException(), R.string.error_message_fetching_entries);
            }
            RecyclerView entryList = findViewById(R.id.entry_list);
            entryList.setAdapter(new EntryListAdapter());
            asyncStop();
        });
    }

    public void refresh() {
        asyncStart();
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenResult -> {
            if (tokenResult.isSuccessful()) {
                Database.setToken(tokenResult.getResult(), dbResult -> {
                    if (dbResult.getException() != null) {
                        AndroidUtils.handleError(EntryListActivity.this,
                                dbResult.getException(), R.string.error_message_updating_token);
                    }
                    fetchEntries();
                });
            } else {
                AndroidUtils.handleError(EntryListActivity.this,
                        tokenResult.getException(), R.string.error_message_updating_token);
                fetchEntries();
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void showAddPopupMenu(MenuItem item) {
        if (getActiveEntryCount() >= MAX_ENTRIES) {
            Toast.makeText(this,
                    getString(R.string.message_max_entries_arg, Integer.toString(MAX_ENTRIES)),
                    Toast.LENGTH_LONG).show();
        } else {
            MenuBuilder menuBuilder = new MenuBuilder(this);
            menuBuilder.setCallback(new MenuBuilder.Callback() {

                @Override
                public boolean onMenuItemSelected(@NonNull MenuBuilder menu, @NonNull MenuItem item) {
                    if (item.getItemId() == R.id.action_add_twitter) {
                        newEntryLauncher.launch(EntryType.TWITTER);
                    } else if (item.getItemId() == R.id.action_add_reddit) {
                        newEntryLauncher.launch(EntryType.REDDIT);
                    }
                    return false;
                }

                @Override
                public void onMenuModeChange(@NonNull MenuBuilder menu) {
                }

            });

            new MenuInflater(this).inflate(R.menu.entry_list_toolbar_add_popup, menuBuilder);
            addMenuHelper = new MenuPopupHelper(this, menuBuilder, findViewById(item.getItemId()));
            addMenuHelper.setForceShowIcon(true);
            addMenuHelper.show();
        }
    }

    private void outOfSync() {
        Toast.makeText(this, R.string.error_message_out_of_sync, Toast.LENGTH_LONG).show();
        fetchEntries();
    }

    private void addEntry(DatabaseEntry entry) {
        int count = getActiveEntryCount();
        if (count < MAX_ENTRIES) {
            entries.add(entry);
            ((RecyclerView) findViewById(R.id.entry_list)).getAdapter().notifyItemInserted(entries.size() - 1);
            if (entry.getEntry().isActive() && count == MAX_ENTRIES - 1) {
                setActiveSwitchesEnabled(false);
            }
        }
    }

    private boolean removeEntry(DatabaseEntry entry) {
        int index = entries.indexOf(entry);
        if (index != -1) {
            entries.remove(index);
            ((RecyclerView) findViewById(R.id.entry_list)).getAdapter().notifyItemRemoved(index);
            if (entry.getEntry().isActive() && getActiveEntryCount() == MAX_ENTRIES - 1) {
                setActiveSwitchesEnabled(true);
            }
            return true;
        } else {
            outOfSync();
            return false;
        }
    }

    private boolean replaceEntry(DatabaseEntry oldEntry, DatabaseEntry newEntry) {
        int index = entries.indexOf(oldEntry);
        if (index != -1) {
            entries.set(index, newEntry);
            ((RecyclerView) findViewById(R.id.entry_list)).getAdapter().notifyItemChanged(index);
            if (oldEntry.getEntry().isActive() != newEntry.getEntry().isActive()) {
                int activeEntryCount = getActiveEntryCount();
                if (oldEntry.getEntry().isActive() && activeEntryCount == MAX_ENTRIES - 1) {
                    setActiveSwitchesEnabled(true);
                } else if (newEntry.getEntry().isActive() && activeEntryCount == MAX_ENTRIES) {
                    setActiveSwitchesEnabled(false);
                }
            }
            return true;
        } else {
            outOfSync();
            return false;
        }
    }

    private void setEntryActive(DatabaseEntry entry, boolean active) {
        if (!entry.getEntry().isActive() && active && getActiveEntryCount() >= MAX_ENTRIES) {
            Toast.makeText(this,
                    getString(R.string.message_max_entries_arg, Integer.toString(MAX_ENTRIES)),
                    Toast.LENGTH_LONG).show();
        } else {
            asyncStart();
            Database.replaceEntry(entry, entry.getEntry().withActive(active), result -> {
                if (result.getData() != null) {
                    if (replaceEntry(entry, result.getData())) {
                        asyncStop();
                    }
                } else if (result.getException() instanceof UserNotSignedInException) {
                    reauthenticate();
                } else {
                    AndroidUtils.handleError(this, result.getException(), R.string.error_message_updating_entry);
                }
            });
        }
    }

    private int getActiveEntryCount() {
        int count = 0;
        for (DatabaseEntry e : entries) {
            if (e.getEntry().isActive()) {
                count++;
            }
        }
        return count;
    }

    private void setActiveSwitchesEnabled(boolean enabled) {
        RecyclerView entryList = findViewById(R.id.entry_list);
        for (int i = 0; i < entryList.getChildCount(); i++) {
            EntryListAdapter.EntryHolder holder = (EntryListAdapter.EntryHolder) entryList.findViewHolderForAdapterPosition(i);
            if (holder != null) {
                if (!holder.activeSwitch.isChecked()) {
                    setActiveSwitchEnabled(holder.activeSwitch, holder.activeSwitchLayout, enabled);
                }
            }
        }
    }

    private void setActiveSwitchEnabled(SwitchCompat activeSwitch, View activeSwitchLayout, boolean enabled) {
        activeSwitch.setEnabled(enabled);
        if (enabled) {
            activeSwitch.setClickable(true);
            activeSwitchLayout.setClickable(false);
            activeSwitchLayout.setOnClickListener(null);
        } else {
            activeSwitch.setClickable(false);
            activeSwitchLayout.setClickable(true);
            activeSwitchLayout.setOnClickListener(view -> Toast.makeText(this,
                    getString(R.string.message_max_entries_arg, Integer.toString(MAX_ENTRIES)),
                    Toast.LENGTH_LONG).show());
        }
    }

    private class EntryListAdapter extends RecyclerView.Adapter<EntryListAdapter.EntryHolder> {

        @NonNull
        @Override
        public EntryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new EntryHolder(getLayoutInflater().inflate(R.layout.layout_entry_row, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull EntryHolder holder, int position) {
            DatabaseEntry dbEntry = entries.get(position);
            Entry entry = dbEntry.getEntry();

            holder.logoView.setImageResource(entry.getType().getIconResource().getDrawableResId());
            holder.logoView.setContentDescription(getString(entry.getType().getIconResource().getStringResId()));
            if (entry.getDescriptionIconResource().getDrawableResId() != 0) {
                holder.descriptionIconView.setVisibility(View.VISIBLE);
                holder.descriptionIconView.setImageResource(entry.getDescriptionIconResource().getDrawableResId());
                holder.descriptionIconView.setContentDescription(getString(entry.getDescriptionIconResource().getStringResId()));
            } else {
                holder.descriptionIconView.setVisibility(View.GONE);
            }
            holder.titleView.setText(entry.getTitle());
            String description = entry.getDescription();
            if (description != null && description.length() > 0) {
                holder.descriptionView.setVisibility(View.VISIBLE);
                holder.descriptionView.setText(description);
            } else {
                holder.descriptionView.setVisibility(View.GONE);
            }
            setActiveSwitchEnabled(holder.activeSwitch, holder.activeSwitchLayout,
                    entry.isActive() || getActiveEntryCount() < MAX_ENTRIES);
            holder.activeSwitch.setOnCheckedChangeListener(null);
            holder.activeSwitch.setChecked(entry.isActive());
            holder.activeSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                    setEntryActive(dbEntry, isChecked));
            holder.layout.setOnClickListener(view -> editEntryLauncher.launch(dbEntry));
        }

        @Override
        public int getItemCount() {
            return entries.size();
        }

        private class EntryHolder extends RecyclerView.ViewHolder {

            View layout;
            ImageView logoView;
            ImageView descriptionIconView;
            TextView titleView;
            TextView descriptionView;
            View activeSwitchLayout;
            SwitchCompat activeSwitch;

            public EntryHolder(@NonNull View itemView) {
                super(itemView);

                layout = itemView.findViewById(R.id.entry_row_layout);
                logoView = itemView.findViewById(R.id.entry_row_logo);
                descriptionIconView = itemView.findViewById(R.id.entry_row_description_icon);
                titleView = itemView.findViewById(R.id.entry_row_title);
                descriptionView = itemView.findViewById(R.id.entry_row_description);
                activeSwitchLayout = itemView.findViewById(R.id.entry_row_active_switch_layout);
                activeSwitch = itemView.findViewById(R.id.entry_row_active_switch);
            }

        }

    }

}
