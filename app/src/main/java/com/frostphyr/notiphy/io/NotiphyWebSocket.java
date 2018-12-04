package com.frostphyr.notiphy.io;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Toast;

import com.frostphyr.notiphy.Entry;
import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.NotificationDispatcher;
import com.frostphyr.notiphy.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class NotiphyWebSocket {

    private static final int OPERATION_ADD = 0;
    private static final int OPERATION_REMOVE = 1;

    private SimpleDateFormat delayDateFormat = new SimpleDateFormat("s", Locale.getDefault());

    private Set<Entry> entries = new HashSet<>();

    private Context context;
    private Handler mainHandler;
    private NotificationDispatcher notificationDispatcher;

    private OkHttpClient client;
    private WebSocket webSocket;
    private ConnectivityManager connectivityManager;
    private long reconnectDelay;
    private long reconnectStart;
    private String reconnectText;
    private boolean reconnectScheduled;
    private boolean wifiOnly;

    public NotiphyWebSocket(Context context, OkHttpClient client) {
        this.context = context;
        this.client = client;

        init();
    }

    private void init() {
        mainHandler = new Handler(context.getMainLooper());
        notificationDispatcher = new NotificationDispatcher(context);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        reconnectDelay = context.getResources().getInteger(R.integer.reconnect_delay);
        reconnectText = context.getString(R.string.error_message_notiphy_connection, delayDateFormat.format(new Date(context.getResources().getInteger(R.integer.reconnect_delay))));

        NetworkMonitor.addListener(context, new NetworkListener() {

            @Override
            public void networkChanged() {
                checkConnection();
            }

        });
    }

    public void setWifiOnly(boolean wifiOnly) {
        if (this.wifiOnly != wifiOnly) {
            this.wifiOnly = wifiOnly;

            checkConnection();
        }
    }

    public NotificationDispatcher getNotificationDispatcher() {
        return notificationDispatcher;
    }

    public void entriesAdded(Collection<Entry> newEntries) {
        entries.addAll(newEntries);
        int[] operations = new int[newEntries.size()];
        for (int i = 0; i < operations.length; i++) {
            operations[i] = OPERATION_ADD;
        }

        entriesModified(newEntries.toArray(new Entry[0]), operations);
    }

    public void entryAdded(Entry entry) {
        if (entries.add(entry)) {
            entriesModified(new Entry[]{entry}, new int[]{OPERATION_ADD});
        }
    }

    public void entryRemoved(Entry entry) {
        if (entries.remove(entry)) {
            entriesModified(new Entry[]{entry}, new int[]{OPERATION_REMOVE});
        }
    }

    public void entryReplaced(Entry oldEntry, Entry newEntry) {
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
            checkConnection();
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
        Toast.makeText(context, reconnectText, Toast.LENGTH_LONG).show();
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
            if (mobileInfo != null && mobileInfo.isConnectedOrConnecting()) {
                return true;
            }
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

    private void processMessage(String message) {
        try {
            JSONObject obj = new JSONObject(message);
            notificationDispatcher.dispatch(EntryType.valueOf(obj.getString("type")).getMessageDecoder().decode(obj));
        } catch (JSONException | IllegalArgumentException e) {
        }
    }

    private WebSocketListener webSocketListener = new WebSocketListener() {

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
                    if (!(reconnectScheduled && SystemClock.elapsedRealtime() - reconnectStart < reconnectDelay)) {
                        postDelayedReconnection();
                    }
                }

            });
        }

    };

}
