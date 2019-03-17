package com.frostphyr.notiphy.twitter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.frostphyr.notiphy.EntryActivity;
import com.frostphyr.notiphy.EntryViewFactory;
import com.frostphyr.notiphy.NotiphyApplication;
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
            holder.active = view.findViewById(R.id.active_switch);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.entry = entry;
        final View finalView = view;

        holder.username.setText(entry.getUsername());
        holder.mediaType.setText(activity.getString(R.string.media_label, entry.getMediaType().toString()));
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

        setEnabled(holder, entry.isActive());
        holder.active.setChecked(entry.isActive());
        holder.active.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ViewHolder holder = (ViewHolder) finalView.getTag();
                setEnabled(holder, isChecked);

                NotiphyApplication application = ((NotiphyApplication) activity.getApplicationContext());
                TwitterEntry oldEntry = holder.entry;
                holder.entry = holder.entry.withActive(isChecked);
                application.replaceEntry(oldEntry, holder.entry);
            }

        });
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, TwitterActivity.class);
                intent.putExtra(EntryActivity.EXTRA_ENTRY, entry);
                activity.startActivity(intent);
            }

        });
        return view;
    }

    private static void setEnabled(ViewHolder holder, boolean enabled) {
        holder.username.setEnabled(enabled);
        holder.mediaType.setEnabled(enabled);
        holder.phrases.setEnabled(enabled);
    }

    private static class ViewHolder {

        TwitterEntry entry;
        TextView username;
        TextView mediaType;
        TextView phrases;
        SwitchCompat active;

    }

}
