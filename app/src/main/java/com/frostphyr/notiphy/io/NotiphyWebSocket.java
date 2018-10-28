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
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class NotiphyWebSocket {

    private static final int OPERATION_ADD = 0;
    private static final int OPERATION_REMOVE = 1;

    private SimpleDateFormat delayDateFormat = new SimpleDateFormat("s");

    private Context context;
    private Handler mainHandler;
    private Set<Entry> entries;
    private NotificationDispatcher notificationDispatcher;

    private OkHttpClient client;
    private WebSocket webSocket;
    private ConnectivityManager connectivityManager;
    private long reconnectDelay;
    private long reconnectStart;
    private String reconnectText;
    private boolean reconnectScheduled;


    public NotiphyWebSocket(Context context, OkHttpClient client, Set<Entry> entries) {
        this.context = context;
        this.client = client;
        this.entries = entries;

        init();
    }

    public void entriesAdded(Collection<Entry> entries) {
        if (webSocket != null) {
            sendEntryOperations(encodeEntries(entries, OPERATION_ADD));
        } else {
            attemptConnection();
        }
    }

    public void entryAdded(Entry entry) {
        if (webSocket != null) {
            sendEntryOperations(encodeEntry(entry, OPERATION_ADD));
        } else {
            attemptConnection();
        }
    }

    public void entryRemoved(Entry entry) {
        if (webSocket != null) {
            if (entries.size() > 0) {
                sendEntryOperations(encodeEntry(entry, OPERATION_REMOVE));
            } else {
                webSocket.cancel();
            }
        }
    }

    public void entryReplaced(Entry oldEntry, Entry newEntry) {
        if (webSocket != null) {
            sendEntryOperations(encodeEntry(oldEntry, OPERATION_REMOVE), encodeEntry(newEntry, OPERATION_ADD));
        } else {
            attemptConnection();
        }
    }

    private void init() {
        mainHandler = new Handler(context.getMainLooper());
        notificationDispatcher = new NotificationDispatcher(context);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        reconnectDelay = context.getResources().getInteger(R.integer.reconnect_delay);
        StringBuilder builder = new StringBuilder(5);
        builder.append(context.getResources().getString(R.string.error_message_notiphy_connection));
        builder.append(' ');
        builder.append(delayDateFormat.format(new Date(context.getResources().getInteger(R.integer.reconnect_delay))));
        builder.append(' ');
        builder.append(context.getResources().getString(R.string.seconds));
        reconnectText = builder.toString();

        NetworkMonitor.addListener(context, new NetworkListener() {

            @Override
            public void networkChanged() {
                attemptConnection();
            }

        });
    }

    private void attemptConnection() {
        if (webSocket == null && isConnected() && (!reconnectScheduled || SystemClock.elapsedRealtime() - reconnectStart > reconnectDelay * 1.5)) {
            Request request = new Request.Builder()
                    .url("ws://frostphyr.com/NotiphyServer/server")
                    .build();
            webSocket = client.newWebSocket(request, new WebSocketListener() {

                @Override
                public void onMessage(WebSocket webSocket, final String message) {
                    processMessage(message);
                }

                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    attemptReconnection();
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    postDelayedReconnection();
                }

            });
            sendEntryOperations(encodeEntries(entries, OPERATION_ADD));
        }

    }

    private void attemptReconnection() {
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                webSocket = null;
                attemptConnection();
            }

        });
    }

    private void postDelayedReconnection() {
        webSocket = null;
        reconnectScheduled = true;
        reconnectStart = SystemClock.elapsedRealtime();
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, reconnectText, Toast.LENGTH_LONG);
            }
        });
        mainHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                reconnectScheduled = false;
                attemptConnection();
            }

        }, reconnectDelay);
    }

    private boolean isConnected() {
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
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
            if (e.isActive()) {
                try {
                    entriesArray.put(encodeEntry(e.getType().getEntryTransportEncoder(), e));
                } catch (JSONException ex) {
                }
            }
        }
        return encodeEntries(entriesArray, operation);
    }

    private JSONObject encodeEntry(Entry entry, int operation) {
        JSONArray array = new JSONArray();
        if (entry.isActive()) {
            try {
                array.put(encodeEntry(entry.getType().getEntryTransportEncoder(), entry));
            } catch (JSONException e) {
            }
        }
        return encodeEntries(array, operation);
    }

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

}
