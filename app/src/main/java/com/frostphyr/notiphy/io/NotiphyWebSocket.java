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

import com.frostphyr.notiphy.Entry;
import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.R;
import com.frostphyr.notiphy.notification.NotificationDispatcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashSet;
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

    private static final int OPERATION_ADD = 0;
    private static final int OPERATION_REMOVE = 1;

    private Set<Entry> entries = new HashSet<>();

    private Status status = Status.DISCONNECTED;

    private Handler mainHandler;
    private OkHttpClient client;
    private WebSocket webSocket;
    private ConnectivityManager connectivityManager;

    private long reconnectStart;
    private long reconnectDelay;
    private boolean reconnectScheduled;
    private boolean wifiOnly;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_ENTRY_ADD:
                    List<Entry> newEntries = intent.getParcelableArrayListExtra(EXTRA_ENTRIES);
                    performOperation(newEntries, OPERATION_ADD);
                    break;
                case ACTION_ENTRY_REMOVE:
                    List<Entry> oldEntries = intent.getParcelableArrayListExtra(EXTRA_ENTRIES);
                    performOperation(oldEntries, OPERATION_REMOVE);
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

    private void performOperation(List<Entry> entries, int operation) {
        if (operation == OPERATION_ADD ? this.entries.addAll(entries) : this.entries.removeAll(entries)) {
            int[] operations = new int[entries.size()];
            for (int i = 0; i < operations.length; i++) {
                operations[i] = operation;
            }
            entriesModified(entries.toArray(new Entry[0]), operations);
        }
    }

    private void replaceEntry(Entry oldEntry, Entry newEntry) {
        if (entries.remove(oldEntry) || entries.add(newEntry)) {
            entriesModified(new Entry[]{oldEntry, newEntry}, new int[]{OPERATION_REMOVE, OPERATION_ADD});
        }
    }

    private void entriesModified(Entry[] entries, int[] operations) {
        if (webSocket != null) {
            if (this.entries.size() > 0) {
                JSONObject[] objects = new JSONObject[entries.length];
                for (int i = 0; i < objects.length; i++) {
                    objects[i] = encodeEntry(entries[i], operations[i]);
                }

                sendEntryOperations(objects);
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

    private void checkConnection() {
        boolean connected = isConnected();
        if (connected && webSocket == null && entries.size() > 0) {
            Request request = new Request.Builder()
                    .url("ws://10.0.0.196:8080/NotiphyServer/server")
                    .build();
            webSocket = client.newWebSocket(request, webSocketListener);
            sendEntryOperations(encodeEntries(entries, OPERATION_ADD));
        } else if ((entries.size() == 0 || !connected) && webSocket != null) {
            disconnect();
        }
    }

    private void disconnect() {
        webSocket.cancel();
        webSocket = null;
    }

    private void postDelayedReconnection() {
        webSocket = null;
        reconnectScheduled = true;
        reconnectStart = SystemClock.elapsedRealtime();
        mainHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                reconnectScheduled = false;
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

    private void sendEntryOperations(JSONObject... operations) {
        if (operations.length > 0) {
            JSONArray array = new JSONArray();
            for (JSONObject o : operations) {
                array.put(o);
            }
            webSocket.send(array.toString());
        }
    }

    private JSONObject encodeEntries(Collection<Entry> entries, int operation) {
        JSONArray entriesArray = new JSONArray();
        for (Entry e : entries) {
            try {
                entriesArray.put(encodeEntry(e.getType().getEntryTransportEncoder(), e));
            } catch (JSONException ex) {
            }
        }
        return encodeEntries(entriesArray, operation);
    }

    private JSONObject encodeEntry(Entry entry, int operation) {
        JSONArray array = new JSONArray();
        try {
            array.put(encodeEntry(entry.getType().getEntryTransportEncoder(), entry));
        } catch (JSONException e) {
        }
        return encodeEntries(array, operation);
    }

    @SuppressWarnings("unchecked")
    private <T extends Entry> JSONObject encodeEntry(JSONEncoder<T> encoder, Entry entry) throws JSONException {
        return encoder.encode((T) entry);
    }

    private JSONObject encodeEntries(JSONArray entries, int operation) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("op", operation);
            obj.put("entries", entries);
            return obj;
        } catch (JSONException e) {
            return null;
        }
    }

    private void processMessage(final String messageJson) {
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                try {
                    JSONObject obj = new JSONObject(messageJson);
                    sendBroadcast(new Intent(ACTION_MESSAGE_RECEIVED)
                            .putExtra(EXTRA_MESSAGE, EntryType.valueOf(obj.getString("type")).getMessageDecoder().decode(obj)));
                } catch (JSONException | IllegalArgumentException e) {
                }
            }

        });
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
                    setStatus(Status.CONNECTED);
                }

            });
        }

        @Override
        public void onMessage(WebSocket webSocket, String message) {
            processMessage(message);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    NotiphyWebSocket.this.webSocket = null;
                    setStatus(Status.DISCONNECTED);
                    checkConnection();
                }

            });
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    NotiphyWebSocket.this.webSocket = null;
                    setStatus(Status.FAILURE);
                    if (!(reconnectScheduled && SystemClock.elapsedRealtime() - reconnectStart < reconnectDelay)) {
                        postDelayedReconnection();
                    }
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
