package com.frostphyr.notiphy.settings;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;

import com.firebase.ui.auth.AuthUI;
import com.frostphyr.notiphy.AndroidUtils;
import com.frostphyr.notiphy.AuthActivity;
import com.frostphyr.notiphy.R;
import com.frostphyr.notiphy.UserNotSignedInException;
import com.frostphyr.notiphy.io.Database;
import com.google.android.gms.ads.AdView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;

public class SettingsActivity extends AppCompatActivity {

    private MenuPopupHelper optionsMenuHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        AdView adView = findViewById(R.id.ad_banner);
        adView.loadAd(AndroidUtils.generateAdRequest());

        MaterialToolbar toolbar = findViewById(R.id.settings_toolbar);
        toolbar.setNavigationOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_options) {
                showOptionsPopupMenu(item);
                return true;
            }
            return false;
        });

        ((ListView) findViewById(R.id.settings_list)).setAdapter(new SettingsAdapter());
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onPause() {
        super.onPause();

        if (optionsMenuHelper != null && optionsMenuHelper.isShowing()) {
            optionsMenuHelper.dismiss();
            optionsMenuHelper = null;
        }
    }

    @SuppressLint("RestrictedApi")
    private void showOptionsPopupMenu(MenuItem item) {
        MenuBuilder menuBuilder = new MenuBuilder(this);
        menuBuilder.setCallback(new MenuBuilder.Callback() {

            @Override
            public boolean onMenuItemSelected(@NonNull MenuBuilder menu, @NonNull MenuItem popupItem) {
                if (popupItem.getItemId() == R.id.action_sign_out) {
                    showConfirmationDialog(R.string.confirmation_logout, (dialog, which) -> signOut());
                } else if (popupItem.getItemId() == R.id.action_delete_account) {
                    showConfirmationDialog(R.string.confirmation_delete_account, (dialog, which) -> deleteAccount());
                } else if (popupItem.getItemId() == R.id.action_support) {
                    AndroidUtils.openUri(SettingsActivity.this, getString(R.string.url_support));
                } else if (popupItem.getItemId() == R.id.action_terms) {
                    AndroidUtils.openUri(SettingsActivity.this, getString(R.string.url_terms));
                } else if (popupItem.getItemId() == R.id.action_privacy) {
                    AndroidUtils.openUri(SettingsActivity.this, getString(R.string.url_privacy));
                } else {
                    return false;
                }
                return true;
            }

            @Override
            public void onMenuModeChange(@NonNull MenuBuilder menu) {
            }

        });

        new MenuInflater(this).inflate(R.menu.settings_toolbar_options_popup, menuBuilder);
        optionsMenuHelper = new MenuPopupHelper(this, menuBuilder, findViewById(item.getItemId()));
        optionsMenuHelper.setForceShowIcon(true);
        optionsMenuHelper.show();
    }

    private void asyncStart() {
        findViewById(R.id.settings_content).setVisibility(View.GONE);
        findViewById(R.id.settings_progress).setVisibility(View.VISIBLE);
    }

    private void asyncStop() {
        findViewById(R.id.settings_progress).setVisibility(View.GONE);
        findViewById(R.id.settings_content).setVisibility(View.VISIBLE);
    }

    private void showConfirmationDialog(int messageResId, DialogInterface.OnClickListener positiveListener) {
        new MaterialAlertDialogBuilder(SettingsActivity.this)
                .setMessage(messageResId)
                .setPositiveButton(R.string.yes, positiveListener)
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void authenticate(boolean returnToCaller) {
        Intent intent = new Intent(this, AuthActivity.class)
                .putExtra(AuthActivity.EXTRA_RETURN_TO_CALLER, returnToCaller);
        if (!returnToCaller) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        startActivity(intent);
        if (!returnToCaller) {
            setResult(RESULT_OK);
            finish();
        }
    }

    private void signOut() {
        asyncStart();
        Database.deleteToken(dbResult -> {
            if (dbResult.getException() == null) {
                AuthUI.getInstance().signOut(this)
                        .addOnCompleteListener(result -> {
                            if (result.isSuccessful()) {
                                authenticate(false);
                            } else {
                                AndroidUtils.handleError(this, result.getException(), R.string.error_message_sign_out);
                                asyncStop();
                            }
                        });
            } else {
                if (dbResult.getException() instanceof UserNotSignedInException) {
                    authenticate(false);
                } else {
                    AndroidUtils.handleError(this, dbResult.getException(), R.string.error_message_sign_out);
                    asyncStop();
                }
            }
        });
    }

    private void deleteAccount() {
        asyncStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseFunctions.getInstance()
                    .getHttpsCallable("deleteUser")
                    .call()
                    .addOnCompleteListener(result -> {
                        if (result.isSuccessful()) {
                            FirebaseAuth.getInstance().signOut();
                            authenticate(false);
                        } else {
                            FirebaseFunctionsException e = (FirebaseFunctionsException) result.getException();
                            if (e.getCode() == FirebaseFunctionsException.Code.UNAUTHENTICATED) {
                                authenticate(true);
                            } else {
                                AndroidUtils.handleError(this, e, R.string.error_message_delete_account);
                            }
                            asyncStop();
                        }
                    });
        } else {
            authenticate(true);
            asyncStop();
        }
    }

    private class SettingsAdapter extends ArrayAdapter<Setting<?>> {

        public SettingsAdapter() {
            super(SettingsActivity.this, -1, Setting.getSettings());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).getViewFactory().create(SettingsActivity.this, parent);
        }

    }

}
