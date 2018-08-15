package com.frostphyr.notiphy.io;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public abstract class IOService extends IntentService {

    public static final String ACTION_EXCEPTION = "exception";

    public static final String EXTRA_STACK_TRACE = "stackTrace";
    public static final String EXTRA_ERROR_MESSAGE_ID = "errorMessageId";

    public IOService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            onHandleIntentThrowable(intent);
        } catch (Exception e) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTION_EXCEPTION);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(EXTRA_ERROR_MESSAGE_ID, getErrorMessageId(intent));
            broadcastIntent.putExtra(EXTRA_STACK_TRACE, Log.getStackTraceString(e));
            sendBroadcast(broadcastIntent);
        }
    }

    protected abstract void onHandleIntentThrowable(Intent intent) throws Exception;

    protected abstract int getErrorMessageId(Intent intent);

}
