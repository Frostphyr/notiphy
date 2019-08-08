package com.frostphyr.notiphy.notification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.frostphyr.notiphy.EntryListActivity;
import com.frostphyr.notiphy.Media;
import com.frostphyr.notiphy.MediaType;
import com.frostphyr.notiphy.Message;
import com.frostphyr.notiphy.NsfwContent;
import com.frostphyr.notiphy.R;
import com.frostphyr.notiphy.StartupActivity;
import com.frostphyr.notiphy.io.ImageDownloader;

import java.text.DateFormat;
import java.util.List;

public class NotificationListActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGES = "com.frostphyr.notiphy.extra.MESSAGES";
    public static final String EXTRA_NSFW_CONTENT_ORDINAL = "com.frostphyr.notiphy.extra.NSFW_CONTENT_ORDINAL";
    public static final String EXTRA_SHOW_MEDIA = "com.frostphyr.notiphy.extra.SHOW_MEDIA";

    private boolean[] expanded;
    private int[] mediaIndex;

    private NsfwContent nsfwContent;
    private boolean showMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        actionBar.setDisplayHomeAsUpEnabled(true);

        nsfwContent = NsfwContent.values()[getIntent().getIntExtra(EXTRA_NSFW_CONTENT_ORDINAL, 0)];
        showMedia = getIntent().getBooleanExtra(EXTRA_SHOW_MEDIA, true);

        List<Message> messages = getIntent().getParcelableArrayListExtra(EXTRA_MESSAGES);
        expanded = new boolean[messages.size()];
        mediaIndex = new int[messages.size()];

        ListView notificationList = findViewById(R.id.notification_list);
        notificationList.setAdapter(new MessageRowAdapter(messages));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getIntent().getBooleanExtra(StartupActivity.EXTRA_IS_APP_LAUNCH, false)) {
                startActivity(new Intent(this, EntryListActivity.class));
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MessageRowAdapter extends ArrayAdapter<Message> {

        private LayoutInflater inflater = getLayoutInflater();

        public MessageRowAdapter(List<Message> messages) {
            super(NotificationListActivity.this, -1, messages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.layout_notification, parent, false);
                holder = new Holder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.index = position;
            holder.message = getItem(position);
            initViews(holder);
            return convertView;
        }

        private void initViews(Holder holder) {
            int textColor = ContextCompat.getColor(NotificationListActivity.this, R.color.light_text);
            int secondaryTextColor = ContextCompat.getColor(NotificationListActivity.this, R.color.light_text_secondary);
            int padding = (int) getResources().getDimension(R.dimen.default_margin);
            holder.view.setPadding(padding, padding, padding, padding);
            holder.view.setOnClickListener(new OpenUrlListener(holder.message.getUrl()));
            holder.iconView.setImageResource(holder.message.getType().getIconResourceId());
            holder.timeView.setPadding(padding, 0, padding, 0);
            setText(holder.view, R.id.notification_title, holder.message.getTitle(), textColor);
            setText(holder.view, R.id.notification_description, holder.message.getDescription(), textColor);
            setText(holder.view, R.id.notification_text, holder.message.getText(), secondaryTextColor);
            setText(holder.view, R.id.notification_time, DateFormat.getTimeInstance(DateFormat.SHORT).format(holder.message.getCreatedAt()), secondaryTextColor);
            holder.mediaView.setImageBitmap(null);

            if (nsfwContent == NsfwContent.HIDE && holder.message.isNsfw()) {
                holder.contentLayout.setVisibility(View.INVISIBLE);
                holder.nsfwOverlay.setVisibility(View.VISIBLE);
                holder.showNsfwButton.setOnClickListener(new ShowNsfwListener(holder));
            } else {
                holder.contentLayout.setVisibility(View.VISIBLE);
                holder.nsfwOverlay.setVisibility(View.GONE);
            }

            if (showMedia && holder.message.getMedia() != null && holder.message.getMedia().length > 0) {
                if (holder.message.getMedia().length == 1) {
                    holder.mediaCountLayout.setVisibility(View.GONE);
                } else {
                    holder.mediaCountLayout.setVisibility(View.VISIBLE);
                    holder.mediaCountView.setTextColor(secondaryTextColor);
                    int color = getResources().getColor(R.color.light_text);
                    holder.previousMediaButton.setColorFilter(color);
                    holder.nextMediaButton.setColorFilter(color);
                }
                holder.mediaLayout.setVisibility(View.VISIBLE);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.mediaView.getLayoutParams();
                params.setMargins(padding, padding, padding, padding);
                new MediaHandler(holder);
            } else {
                holder.mediaLayout.setVisibility(View.GONE);
                holder.mediaCountLayout.setVisibility(View.GONE);
            }

            holder.expandButton.setOnClickListener(new ExpandListener(holder));
            if (expanded[holder.index]) {
                expandMore(holder);
            } else {
                expandLess(holder);
            }
        }

        private void setText(View parent, int viewId, CharSequence text, int color) {
            TextView view = parent.findViewById(viewId);
            if (text == null || text.equals("")) {
                view.setVisibility(View.GONE);
            } else {
                view.setVisibility(View.VISIBLE);
                view.setText(text);
                view.setTextColor(color);
            }
        }

        private void expandMore(Holder holder) {
            holder.expandButton.setImageResource(R.drawable.ic_expand_less);
            holder.expandButton.setContentDescription(getString(R.string.expand_more));
            holder.textView.setMaxLines(Integer.MAX_VALUE);
            if (showMedia && holder.message.getMedia() != null && holder.message.getMedia().length > 0) {
                holder.mediaLayout.setVisibility(View.VISIBLE);
                if (holder.message.getMedia().length > 1) {
                    holder.mediaCountLayout.setVisibility(View.VISIBLE);
                }
            }
        }

        private void expandLess(Holder holder) {
            holder.expandButton.setImageResource(R.drawable.ic_expand_more);
            holder.expandButton.setContentDescription(getString(R.string.expand_less));
            holder.textView.setMaxLines(2);
            holder.mediaLayout.setVisibility(View.GONE);
            holder.mediaCountLayout.setVisibility(View.GONE);
        }

        private class Holder {

            int index;
            Message message;
            View view;
            View contentLayout;
            View mediaLayout;
            View mediaCountLayout;
            View nsfwOverlay;
            Button showNsfwButton;
            ImageView iconView;
            ImageView mediaView;
            ImageView mediaIconView;
            ImageButton expandButton;
            ImageButton previousMediaButton;
            ImageButton nextMediaButton;
            TextView titleView;
            TextView descriptionView;
            TextView textView;
            TextView timeView;
            TextView mediaCountView;
            ProgressBar mediaLoadingView;

            public Holder(View view) {
                this.view = view;
                contentLayout = view.findViewById(R.id.notification_content);
                mediaLayout = view.findViewById(R.id.notification_media_layout);
                mediaCountLayout = view.findViewById(R.id.notification_media_count_layout);
                nsfwOverlay = view.findViewById(R.id.notification_nsfw_overlay);
                showNsfwButton = view.findViewById(R.id.notification_show_nsfw);
                iconView = view.findViewById(R.id.notification_icon);
                mediaView = view.findViewById(R.id.notification_media_image);
                mediaIconView = view.findViewById(R.id.notification_media_icon);
                expandButton = view.findViewById(R.id.notification_expand);
                previousMediaButton = view.findViewById(R.id.notification_media_previous_button);
                nextMediaButton = view.findViewById(R.id.notification_media_next_button);
                titleView = view.findViewById(R.id.notification_title);
                descriptionView = view.findViewById(R.id.notification_description);
                textView = view.findViewById(R.id.notification_text);
                timeView = view.findViewById(R.id.notification_time);
                mediaCountView = view.findViewById(R.id.notification_media_count);
                mediaLoadingView = view.findViewById(R.id.notification_media_loading);
            }

        }

        private class ExpandListener implements View.OnClickListener {

            private Holder holder;

            public ExpandListener(Holder holder) {
                this.holder = holder;
            }

            @Override
            public void onClick(View v) {
                if (expanded[holder.index]) {
                    expanded[holder.index] = false;
                    expandLess(holder);
                } else {
                    expanded[holder.index] = true;
                    expandMore(holder);
                }
            }

        }

        private class OpenUrlListener implements View.OnClickListener {

            private Uri uri;

            public OpenUrlListener(String url) {
                uri = Uri.parse(url);
            }

            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }

        }

        private class ShowNsfwListener implements View.OnClickListener {

            private Holder holder;

            public ShowNsfwListener(Holder holder) {
                this.holder = holder;
            }

            @Override
            public void onClick(View v) {
                holder.contentLayout.setVisibility(View.VISIBLE);
                holder.nsfwOverlay.setVisibility(View.GONE);
            }

        }

        private class MediaHandler {

            private Holder holder;
            private int index;

            public MediaHandler(Holder holder) {
                this.holder = holder;
                index = holder.index;

                init();
            }

            private void init() {
                holder.mediaView.setOnClickListener(openMediaListener);
                holder.previousMediaButton.setOnClickListener(navigationListener);
                holder.nextMediaButton.setOnClickListener(navigationListener);
                update();
            }

            private void update() {
                if (holder.message.getMedia().length > 1) {
                    holder.mediaCountView.setText((mediaIndex[index] + 1) + "/" + holder.message.getMedia().length);
                }
                holder.mediaView.setVisibility(View.INVISIBLE);
                holder.mediaIconView.setVisibility(View.INVISIBLE);
                holder.mediaLoadingView.setVisibility(View.VISIBLE);
                holder.previousMediaButton.setEnabled(false);
                holder.nextMediaButton.setEnabled(false);
                downloadImage();
            }

            private void downloadImage() {
                final Media media = holder.message.getMedia()[mediaIndex[index]];
                ImageDownloader.execute(media.getThumbnailUrl(), new ImageDownloader.Callback() {

                    @Override
                    public void onDownload(Bitmap bitmap) {
                        if (index == holder.index) {
                            holder.mediaView.setImageBitmap(bitmap);
                            holder.mediaView.setVisibility(View.VISIBLE);
                            if (media.getType() == MediaType.VIDEO) {
                                holder.mediaView.setContentDescription(getString(R.string.video));
                                holder.mediaIconView.setContentDescription(getString(R.string.play));
                                holder.mediaIconView.setImageResource(R.drawable.ic_play);
                                holder.mediaIconView.setVisibility(View.VISIBLE);
                            } else {
                                holder.mediaView.setContentDescription(getString(R.string.image));
                            }
                            onResult();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (index == holder.index) {
                            holder.mediaIconView.setContentDescription(getString(R.string.error));
                            holder.mediaIconView.setImageResource(R.drawable.ic_error);
                            holder.mediaIconView.setVisibility(View.VISIBLE);
                            onResult();
                        }
                    }

                    private void onResult() {
                        holder.mediaLoadingView.setVisibility(View.GONE);
                        holder.previousMediaButton.setEnabled(true);
                        holder.nextMediaButton.setEnabled(true);
                    }

                });
            }

            private View.OnClickListener openMediaListener = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (index == holder.index) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(holder.message.getMedia()[mediaIndex[index]].getUrl())));
                    }
                }

            };

            private View.OnClickListener navigationListener = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (v.getId() == R.id.notification_media_previous_button) {
                        if (--mediaIndex[index] < 0) {
                            mediaIndex[index] = holder.message.getMedia().length - 1;
                        }
                    } else if (v.getId() == R.id.notification_media_next_button) {
                        if (++mediaIndex[index] >= holder.message.getMedia().length) {
                            mediaIndex[index] = 0;
                        }
                    }
                    update();
                }

            };

        }

    }

}
