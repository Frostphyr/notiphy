package com.frostphyr.notiphy;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.frostphyr.notiphy.settings.Setting;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AgreementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_loading);

        if (!Setting.CRASH_REPORTING.isSet(this)) {
            AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                    .setCancelable(false)
                    .setTitle(R.string.crash_reporting)
                    .setMessage(R.string.agreement_crash_reporting)
                    .setPositiveButton(R.string.yes, (d, which) -> setCrashReporting(true))
                    .setNegativeButton(R.string.no, (d, which) -> setCrashReporting(false))
                    .create();
            dialog.show();
            ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            finishAgreement();
        }
    }

    private void setCrashReporting(boolean value) {
        Setting.CRASH_REPORTING.set(this, value);
        finishAgreement();
    }

    private void finishAgreement() {
        startActivity(new Intent(this, AuthActivity.class));
        setResult(RESULT_OK);
        finish();
    }
    
}
