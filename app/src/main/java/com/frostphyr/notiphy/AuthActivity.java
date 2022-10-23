package com.frostphyr.notiphy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.frostphyr.notiphy.io.Database;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;

public class AuthActivity extends AppCompatActivity {

    public static final String EXTRA_RETURN_TO_CALLER = "com.frostphyr.notiphy.extra.RETURN_TO_CALLER";
    public static final String EXTRA_FINISH_INTENT = "com.frostphyr.notiphy.extra.FINISH_INTENT";

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(), this::onSignIn);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        findViewById(R.id.auth_sign_in).setOnClickListener(v -> startSignIn());

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            finishSignIn();
        } else {
            if (getIntent().getBooleanExtra(EXTRA_RETURN_TO_CALLER, false)) {
                Toast.makeText(this, R.string.error_message_signed_out, Toast.LENGTH_LONG).show();
            }

            startSignIn();
        }
    }

    private void onSignIn(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            finishSignIn();
        } else {
            if (result.getIdpResponse() != null && result.getIdpResponse().getError() != null) {
                AndroidUtils.handleError(this, result.getIdpResponse().getError(), R.string.error_message_sign_in);
                findViewById(R.id.auth_sign_in).setVisibility(View.VISIBLE);
                findViewById(R.id.auth_progress).setVisibility(View.GONE);
            } else {
                startSignIn();
            }
        }
    }

    private void startSignIn() {
        AuthUI.getInstance().signOut(this).addOnCompleteListener(result ->
                signInLauncher.launch(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                                .setAvailableProviders(Arrays.asList(
                                        new AuthUI.IdpConfig.EmailBuilder().setRequireName(false).build(),
                                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                                        new AuthUI.IdpConfig.TwitterBuilder().build(),
                                        new AuthUI.IdpConfig.FacebookBuilder().build()))
                                .setIsSmartLockEnabled(false)
                                .setTheme(R.style.NotiphyTheme_FirebaseUI)
                                .setTosAndPrivacyPolicyUrls(getString(R.string.url_terms), getString(R.string.url_privacy))
                                .build()));
    }

    private void finishSignIn() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(result -> {
                    if (result.isSuccessful()) {
                        Database.setToken(result.getResult(), dbResult -> {
                            if (dbResult.getException() == null) {
                                startNextActivity();
                            } else {
                                AndroidUtils.handleError(this, dbResult.getException(), R.string.error_message_sign_in);
                                reset();
                            }
                        });
                    } else {
                        AndroidUtils.handleError(this, result.getException(), R.string.error_message_sign_in);
                        reset();
                    }
                });
    }

    private void reset() {
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(result -> startSignIn());
    }

    private void startNextActivity() {
        Intent intent = getIntent();
        boolean returnToCaller = intent.getBooleanExtra(EXTRA_RETURN_TO_CALLER, false);
        if (intent.hasExtra(EXTRA_FINISH_INTENT)) {
            Intent finishIntent = intent.getParcelableExtra(EXTRA_FINISH_INTENT);
            startActivity(finishIntent
                    .putExtra(EXTRA_RETURN_TO_CALLER, returnToCaller));
        } else if (!returnToCaller) {
            startActivity(new Intent(this, EntryListActivity.class));
        }

        setResult(RESULT_OK);
        finish();
    }

}
