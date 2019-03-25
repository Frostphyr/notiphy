package com.frostphyr.notiphy.twitter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frostphyr.notiphy.EntryActivity;
import com.frostphyr.notiphy.EntryViewFactory;
import com.frostphyr.notiphy.R;

public class TwitterEntryViewFactory implements EntryViewFactory<TwitterEntry> {

    @Override
    public View createView(final TwitterEntry entry, LayoutInflater inflater, View view, ViewGroup parent, final Activity activity) {
        ViewHolder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.layout_entry_row_twitter, parent, false);
            holder = new ViewHolder();
            holder.username = view.findViewById(R.id.username);
            holder.mediaType = view.findViewById(R.id.media_type);
            holder.phrases = view.findViewById(R.id.phrases);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.entry = entry;

        holder.username.setText(entry.getUsername());
        int mediaTypeResId = entry.getMediaType().getIconResId();
        if (mediaTypeResId != -1) {
            holder.mediaType.setImageResource(mediaTypeResId);
            holder.mediaType.setVisibility(View.VISIBLE);
        } else {
            holder.mediaType.setVisibility(View.GONE);
        }
        String[] phrases = entry.getPhrases();
        if (phrases.length > 0) {
            StringBuilder builder = new StringBuilder(Math.max(phrases.length * 2 - 1, 0));
            for (int i = 0; i < phrases.length; i++) {
                builder.append(phrases[i]);
                if (i != phrases.length - 1) {
                    builder.append(", ");
                }
            }
            holder.phrases.setText(builder.toString());
        } else {
            holder.phrases.setVisibility(View.GONE);
        }

        parent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, TwitterActivity.class);
                intent.putExtra(EntryActivity.EXTRA_ENTRY, entry);
                activity.startActivityForResult(intent, TwitterActivity.REQUEST_CODE_EDIT);
            }

        });
        return view;
    }

    private static class ViewHolder {

        TwitterEntry entry;
        TextView username;
        ImageView mediaType;
        TextView phrases;

    }

}
