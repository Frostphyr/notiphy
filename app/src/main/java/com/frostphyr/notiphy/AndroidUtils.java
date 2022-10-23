package com.frostphyr.notiphy;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.frostphyr.notiphy.settings.Setting;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.IOException;
import java.net.URL;

public class AndroidUtils {

    public static void handleError(Context context, Throwable t, int messageResId) {
        handleError(context, t);
        Toast.makeText(context, messageResId, Toast.LENGTH_LONG).show();
    }

    public static void handleError(Context context, Throwable t) {
        if (Setting.CRASH_REPORTING.get(context)) {
            FirebaseCrashlytics.getInstance().recordException(t);
        }
    }

    public static void showDialog(Context context, int titleResId, int messageResId) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(titleResId)
                .setMessage(messageResId)
                .setPositiveButton(R.string.ok, null)
                .create();
        dialog.show();
        ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static AdRequest generateAdRequest() {
        Bundle extras = new Bundle();
        extras.putString("npa", "1");

        return new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();
    }

    public static Bitmap downloadMedia(Media media, int maxWidth, int maxHeight) {
        int sampleWidth = Math.min(maxWidth, media.getWidth());
        int sampleHeight = Math.min(Math.min(maxHeight, media.getHeight()), media.getWidth() / 2);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(media, sampleWidth, sampleHeight);
        try {
            return BitmapFactory.decodeStream(new URL(media.getThumbnailUrl()).openStream(), null, options);
        } catch (IOException e) {
            return null;
        }
    }

    public static void downloadMedia(Media media, int maxWidth, int maxHeight, Callback<Bitmap> callback) {
        AsyncExecutor.execute(new AsyncExecutor.Task<Bitmap>() {

            @Override
            public Bitmap execute() throws Exception {
                return downloadMedia(media, maxWidth, maxHeight);
            }

            @Override
            public void handleResult(Bitmap result) {
                callback.onComplete(new Callback.Result<>(result));
            }

            @Override
            public void handleException(Exception exception) {
                callback.onComplete(new Callback.Result<>(exception));
            }

        });
    }

    private static int calculateInSampleSize(Media media, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (media.getHeight() > reqHeight || media.getWidth() > reqWidth) {
            int halfHeight = media.getHeight() / 2;
            int halfWidth = media.getWidth() / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static void openUri(Context context, String uri) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.error_message_opening_url, Toast.LENGTH_LONG).show();
        }
    }

    public static <T extends SpinnerItem> ArrayAdapter<T> createSpinnerAdapter(Context context, T[] objects) {
        ArrayAdapter<T> adapter = new SpinnerAdapter<T>(context, R.layout.layout_spinner_item, objects);
        adapter.setDropDownViewResource(R.layout.layout_spinner_dropdown_item);
        return adapter;
    }

    private static class SpinnerAdapter<T extends SpinnerItem> extends ArrayAdapter<T> {

        private final LayoutInflater inflater;

        public SpinnerAdapter(@NonNull Context context, int resource, @NonNull T[] objects) {
            super(context, resource, objects);

            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return createView(position, convertView, parent, R.layout.layout_spinner_item);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return createView(position, convertView, parent, R.layout.layout_spinner_dropdown_item);
        }

        private View createView(int position, View convertView, ViewGroup parent, int layoutResId) {
            T item = getItem(position);
            View view = convertView == null ? inflater.inflate(layoutResId, parent, false) : convertView;
            TextView textView = view.findViewById(R.id.spinner_item_text);
            textView.setText(item.getIconResource().getStringResId());
            ImageView imageView = view.findViewById(R.id.spinner_item_image);
            if (item.getIconResource().getDrawableResId() != 0) {
                imageView.setImageResource(item.getIconResource().getDrawableResId());
                imageView.setContentDescription(getContext().getString(item.getIconResource().getStringResId()));
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.GONE);
            }
            return view;
        }

    }

}
