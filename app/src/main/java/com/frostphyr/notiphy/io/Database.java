package com.frostphyr.notiphy.io;

import com.frostphyr.notiphy.Callback;
import com.frostphyr.notiphy.DatabaseEntry;
import com.frostphyr.notiphy.Entry;
import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.UserNotSignedInException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    public static void setToken(String token, Callback<Void> callback) {
        DocumentReference userDoc = getUserDocument(callback);
        if (userDoc != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("value", token);
            data.put("timestamp", System.currentTimeMillis());
            Task<Void> task = userDoc.collection("info")
                    .document("token")
                    .set(data);
            if (callback != null) {
                task.addOnSuccessListener(doc -> callback.onComplete(new Callback.Result<>()))
                        .addOnFailureListener(e -> callback.onComplete(new Callback.Result<>(e)));
            }
        }
    }

    public static void deleteToken(Callback<Void> callback) {
        DocumentReference userDoc = getUserDocument(callback);
        if (userDoc != null) {
            Task<Void> task = userDoc.collection("info")
                    .document("token")
                    .delete();
            if (callback != null) {
                task.addOnSuccessListener(doc -> callback.onComplete(new Callback.Result<>()))
                        .addOnFailureListener(e -> callback.onComplete(new Callback.Result<>(e)));
            }
        }
    }

    public static void getEntries(Callback<List<DatabaseEntry>> callback) {
        readCollection("entries", callback, collection -> {
            List<DatabaseEntry> entries = new ArrayList<>(collection.size());
            for (QueryDocumentSnapshot document : collection) {
                Entry entry = document.toObject(EntryType.valueOf(document.getString("type")).getEntryClass());
                entries.add(new DatabaseEntry(entry, document.getId()));
            }
            Collections.sort(entries, (o1, o2) -> {
                return Long.compare(o1.getEntry().getTimestamp(), o2.getEntry().getTimestamp());
            });

            if (callback != null) {
                callback.onComplete(new Callback.Result<>(entries));
            }
        });
    }

    public static void addEntry(Entry entry, Callback<DatabaseEntry> callback) {
        DocumentReference userDoc = getUserDocument(callback);
        if (userDoc != null) {
            Task<DocumentReference> task = userDoc.collection("entries")
                    .add(entry);
            if (callback != null) {
                task.addOnSuccessListener(doc ->
                                callback.onComplete(new Callback.Result<>(new DatabaseEntry(entry, doc.getId()))))
                        .addOnFailureListener(e -> callback.onComplete(new Callback.Result<>(e)));
            }
        }
    }

    public static void deleteEntry(DatabaseEntry entry, Callback<Void> callback) {
        delete("entries", entry.getId(), callback);
    }

    public static void replaceEntry(DatabaseEntry oldEntry, Entry newEntry,
            Callback<DatabaseEntry> callback) {
        DocumentReference userDoc = getUserDocument(callback);
        if (userDoc != null) {
            Task<Void> task = userDoc.collection("entries")
                    .document(oldEntry.getId())
                    .set(newEntry);
            if (callback != null) {
                task.addOnSuccessListener(doc ->
                                callback.onComplete(new Callback.Result<>(new DatabaseEntry(newEntry, oldEntry.getId()))))
                        .addOnFailureListener(e -> callback.onComplete(new Callback.Result<>(e)));
            }
        }
    }

    private static <T> String getUid(Callback<T> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        } else if (callback != null) {
            callback.onComplete(new Callback.Result<T>(new UserNotSignedInException()));
        }
        return null;
    }

    private static <T> void readCollection(String collection, Callback<T> callback,
            OnSuccessListener<QuerySnapshot> listener) {
        DocumentReference doc = getUserDocument(callback);
        if (doc != null) {
            Task<QuerySnapshot> task = doc.collection(collection)
                    .get()
                    .addOnSuccessListener(listener);
            if (callback != null) {
                task.addOnFailureListener(e -> callback.onComplete(new Callback.Result<T>(e)));
            }
        }
    }

    private static <T> void delete(String collection, String document, Callback<T> callback) {
        DocumentReference doc = getUserDocument(callback);
        if (doc != null) {
            Task<Void> task = doc.collection(collection)
                    .document(document)
                    .delete();
            if (callback != null) {
                task.addOnSuccessListener(unused -> callback.onComplete(new Callback.Result<T>()))
                        .addOnFailureListener(e -> callback.onComplete(new Callback.Result<T>(e)));
            }
        }
    }

    private static <T> DocumentReference getUserDocument(Callback<T> callback) {
        String uid = getUid(callback);
        if (uid != null) {
            return FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid);
        }
        return null;
    }

}
