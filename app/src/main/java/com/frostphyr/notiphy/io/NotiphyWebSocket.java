package com.frostphyr.notiphy.io;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.frostphyr.notiphy.BuildConfig;
import com.frostphyr.notiphy.Entry;
import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.R;
import com.frostphyr.notiphy.notification.NotificationDispatcher;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class NotiphyWebSocket extends Service {

    public static final String ACTION_ENTRY_ADD = "com.frostphyr.notiphy.action.ENTRY_ADD";
    public static final String ACTION_ENTRY_REMOVE = "com.frostphyr.notiphy.action.ENTRY_REMOVE";
    public static final String ACTION_ENTRY_REPLACE = "com.frostphyr.notiphy.action.ENTRY_REPLACE";
    public static final String ACTION_WIFI_ONLY = "com.frostphyr.notiphy.action.WIFI_ONLY";
    public static final String ACTION_GET_STATUS = "com.frostphyr.notiphy.action.GET_STATUS";
    public static final String ACTION_MESSAGE_RECEIVED = "com.frostphyr.notiphy.action.MESSAGE_RECEIVED";
    public static final String ACTION_STATUS_CHANGED = "com.frostphyr.notiphy.action.STATUS_CHANGED";

    public static final String EXTRA_ENTRIES = "com.frostphyr.notiphy.extra.ENTRIES";
    public static final String EXTRA_OLD_ENTRY = "com.frostphyr.notiphy.extra.OLD_ENTRY";
    public static final String EXTRA_NEW_ENTRY = "com.frostphyr.notiphy.extra.NEW_ENTRY";
    public static final String EXTRA_WIFI_ONLY = "com.frostphyr.notiphy.extra.WIFI_ONLY";
    public static final String EXTRA_MESSAGE = "com.frostphyr.notiphy.extra.MESSAGE";
    public static final String EXTRA_STATUS_ORDINAL = "com.frostphyr.notiphy.extra.STATUS_ORDINAL";

    private Set<Entry> entries = new HashSet<>();
    private Status status = Status.DISCONNECTED;

    private Handler mainHandler;
    private OkHttpClient client;
    private WebSocket webSocket;
    private ConnectivityManager connectivityManager;

    private long reconnectStart;
    private long reconnectDelay;
    private boolean wifiOnly;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_ENTRY_ADD:
                    List<Entry> newEntries = intent.getParcelableArrayListExtra(EXTRA_ENTRIES);
                    modifyAll(newEntries, true);
                    break;
                case ACTION_ENTRY_REMOVE:
                    List<Entry> oldEntries = intent.getParcelableArrayListExtra(EXTRA_ENTRIES);
                    modifyAll(oldEntries, false);
                    break;
                case ACTION_ENTRY_REPLACE:
                    Entry oldEntry = intent.getParcelableExtra(EXTRA_OLD_ENTRY);
                    Entry newEntry = intent.getParcelableExtra(EXTRA_NEW_ENTRY);
                    replaceEntry(oldEntry, newEntry);
                    break;
                case ACTION_WIFI_ONLY:
                    boolean wifiOnly = intent.getBooleanExtra(EXTRA_WIFI_ONLY, false);
                    if (this.wifiOnly != wifiOnly) {
                        this.wifiOnly = wifiOnly;
                        checkConnection();
                    }
                    break;
                case ACTION_GET_STATUS:
                    broadcastStatus();
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NotificationDispatcher.ID_FOREGROUND, NotificationDispatcher.createForegroundNotification(this));
        }

        mainHandler = new Handler(getMainLooper());
        client = new OkHttpClient();
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        reconnectDelay = getResources().getInteger(R.integer.reconnect_delay);

        NetworkMonitor.addListener(this, new NetworkListener() {

            @Override
            public void networkChanged() {
                checkConnection();
            }

        });
    }

    @Override
    public void onDestroy() {
        if (webSocket != null) {
            disconnect();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void modifyAll(List<Entry> modifiedEntries, boolean add) {
        for (Iterator<Entry> it = modifiedEntries.iterator(); it.hasNext(); ) {
            if (!(add ? entries.add(it.next()) : entries.remove(it.next()))) {
                it.remove();
            }
        }
        if (modifiedEntries.size() > 0) {
            entriesModified(new EntryOperation(add ? modifiedEntries : null, add ? null : modifiedEntries));
        }
    }

    private void replaceEntry(Entry oldEntry, Entry newEntry) {
        if (!entries.remove(oldEntry)) {
            oldEntry = null;
        }
        if (!entries.add(newEntry)) {
            newEntry = null;
        }

        if (oldEntry != null || newEntry != null) {
            entriesModified(new EntryOperation(Collections.singletonList(newEntry), Collections.singletonList(oldEntry)));
        }
    }

    private void entriesModified(EntryOperation operation) {
        if (webSocket != null) {
            if (this.entries.size() > 0) {
                webSocket.send(EntryOperationEncoder.encode(operation).toString());
            } else {
                disconnect();
            }
        } else {
            if (this.entries.size() > 0) {
                checkConnection();
            } else {
                setStatus(Status.DISCONNECTED);
            }
        }
    }

    private boolean checkConnection() {
        boolean connected = isConnected();
        if (connected && webSocket == null && entries.size() > 0) {
            Request request = new Request.Builder()
                    .url(getString(R.string.server_url))
                    .build();
            webSocket = client.newWebSocket(request, webSocketListener);
            webSocket.send(EntryOperationEncoder.encode(new EntryOperation(entries, null)).toString());
            return true;
        } else if ((entries.size() == 0 || !connected) && webSocket != null) {
            disconnect();
        }
        return false;
    }

    private void disconnect() {
        webSocket.cancel();
        webSocket = null;
    }

    private void postDelayedReconnection() {
        webSocket = null;
        reconnectStart = SystemClock.elapsedRealtime();
        mainHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                checkConnection();
            }

        }, reconnectDelay);
    }

    private boolean isConnected() {
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnectedOrConnecting()) {
            return true;
        }

        if (!wifiOnly) {
            NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            return mobileInfo != null && mobileInfo.isConnectedOrConnecting();
        }
        return false;
    }

    private void onConnect() {
        reconnectStart = 0;
        setStatus(Status.CONNECTED);
    }

    private void onDisconnect() {
        webSocket = null;
        setStatus(Status.DISCONNECTED);
        checkConnection();
    }

    private void onFailure(Throwable t) {
        webSocket = null;
        if (reconnectStart == 0) {
            reconnectStart = -1;
            if (!checkConnection()) {
                setStatus(Status.FAILURE);
                postDelayedReconnection();
            }
        } else if (reconnectStart == -1 || SystemClock.elapsedRealtime() - reconnectStart >= reconnectDelay) {
            setStatus(Status.FAILURE);
            postDelayedReconnection();
        }

        if (BuildConfig.DEBUG) {
            Log.d(getClass().getSimpleName() + "#onFailure", Log.getStackTraceString(t));
        }
    }

    private void processMessage(final String messageJson) {
        try {
            JSONObject obj = new JSONObject(messageJson);
            sendBroadcast(new Intent(ACTION_MESSAGE_RECEIVED)
                    .putExtra(EXTRA_MESSAGE, EntryType.valueOf(obj.getString("type")).getMessageDecoder().decode(obj)));
        } catch (JSONException | IllegalArgumentException e) {
            if (BuildConfig.DEBUG) {
                Log.d(getClass().getSimpleName() + "#processMessage", Log.getStackTraceString(e));
            }
        }
    }

    private void setStatus(Status status) {
        if (this.status != status) {
            this.status = status;
            broadcastStatus();
        }
    }

    private void broadcastStatus() {
        sendBroadcast(new Intent(ACTION_STATUS_CHANGED)
                .putExtra(EXTRA_STATUS_ORDINAL, status.ordinal()));
    }

    private WebSocketListener webSocketListener = new WebSocketListener() {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    onConnect();
                }

            });
        }

        @Override
        public void onMessage(WebSocket webSocket, final String message) {
            mainHandler.post(new Runnable() {

                 @Override
                 public void run() {
                     processMessage(message);
                 }

             });
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    onDisconnect();
                }

            });
        }

        @Override
        public void onFailure(WebSocket webSocket, final Throwable t, Response response) {
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    NotiphyWebSocket.this.onFailure(t);
                }

            });
        }

    };

    public enum Status {

        CONNECTED,
        DISCONNECTED,
        FAILURE

    }

}
