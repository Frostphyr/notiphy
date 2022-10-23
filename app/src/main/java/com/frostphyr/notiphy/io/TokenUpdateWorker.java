package com.frostphyr.notiphy.io;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;

import com.frostphyr.notiphy.UserNotSignedInException;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.TimeUnit;

public class TokenUpdateWorker extends ListenableWorker {

    public TokenUpdateWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    public static void schedule(Context context) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(TokenUpdateWorker.class,
                7, TimeUnit.DAYS).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("TokenUpdateWorker",
                ExistingPeriodicWorkPolicy.KEEP, request);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenResult -> {
                if (tokenResult.isSuccessful()) {
                    Database.setToken(tokenResult.getResult(), dbResult -> {
                        if (dbResult.getException() == null ||
                                dbResult.getException() instanceof UserNotSignedInException) {
                            completer.set(Result.success());
                        } else {
                            completer.set(Result.retry());
                            completer.setException(dbResult.getException());
                        }
                    });
                } else {
                    completer.setException(tokenResult.getException());
                }
            });
            return TokenUpdateWorker.class;
        });
    }

}
