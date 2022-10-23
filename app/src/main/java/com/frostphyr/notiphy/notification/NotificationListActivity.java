package com.frostphyr.notiphy.notification;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.frostphyr.notiphy.AgreementActivity;
import com.frostphyr.notiphy.AndroidUtils;
import com.frostphyr.notiphy.AsyncExecutor;
import com.frostphyr.notiphy.IconResource;
import com.frostphyr.notiphy.MatureContent;
import com.frostphyr.notiphy.R;
import com.frostphyr.notiphy.settings.Setting;
import com.google.android.gms.ads.AdView;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.DateFormat;
import java.util.List;
import java.util.ListIterator;

public class NotificationListActivity extends AppCompatActivity {

    public static final String EXTRA_URLS = "com.frostphyr.notiphy.extra.URLS";

    private MatureContent matureContent;
    private boolean showMedia;
    private boolean[] expanded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);

        AdView adView = findViewById(R.id.ad_banner);
        adView.loadAd(AndroidUtils.generateAdRequest());

        matureContent = Setting.MATURE_CONTENT.get(this);
        showMedia = Setting.SHOW_MEDIA.get(this);

        RecyclerView notificationList = findViewById(R.id.notification_list);
        notificationList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBack();
            }
        });

        AsyncExecutor.execute(new AsyncExecutor.Task<List<Message>>() {

            @Override
            public List<Message> execute() {
                List<String> urls = getIntent().getStringArrayListExtra(EXTRA_URLS);
                MessageDao dao = MessageDao.getInstance(NotificationListActivity.this);
                List<Message> messages = dao.getAll(urls);
                dao.deleteAll(urls);
                return messages;
            }

            @Override
            public void handleResult(List<Message> result) {
                expanded = new boolean[result.size()];

                RecyclerView.Adapter<RecyclerView.ViewHolder> adapter = new MessageListAdapter(result);
                notificationList.setAdapter(adapter);

                MaterialToolbar toolbar = findViewById(R.id.notification_list_toolbar);
                toolbar.setNavigationOnClickListener(view -> navigateBack());
                if (matureContent == MatureContent.HIDE && containsMatureMessage(result)) {
                    toolbar.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.action_visibility) {
                            matureContent = matureContent == MatureContent.HIDE ?
                                    MatureContent.SHOW : MatureContent.HIDE;
                            notifyMatureMessagesChanged(adapter, result);
                            return true;
                        }
                        return false;
                    });
                } else {
                    toolbar.getMenu().removeItem(R.id.action_visibility);
                }

                findViewById(R.id.notification_list_progress).setVisibility(View.GONE);
                findViewById(R.id.notification_list_content).setVisibility(View.VISIBLE);
            }

            @Override
            public void handleException(Exception exception) {
            }

        });
    }

    private void navigateBack() {
        if (isTaskRoot()) {
            startActivity(new Intent(this, AgreementActivity.class));
        }
        finish();
    }

    private boolean containsMatureMessage(List<Message> messages) {
        for (Message m : messages) {
            if (m.mature) {
                return true;
            }
        }
        return false;
    }

    private void notifyMatureMessagesChanged(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter,
                                             List<Message> messages) {
        for (ListIterator<Message> it = messages.listIterator(); it.hasNext(); ) {
            if (it.next().mature) {
                adapter.notifyItemChanged(it.previousIndex());
            }
        }
    }

    private class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_MESSAGE = 0;
        private static final int TYPE_HIDDEN = 1;

        private final List<Message> messages;

        public MessageListAdapter(List<Message> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            return matureContent == MatureContent.HIDE && messages.get(position).mature ?
                    TYPE_HIDDEN : TYPE_MESSAGE;
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { ;
            View view = getLayoutInflater().inflate(viewType == TYPE_HIDDEN ?
                            R.layout.layout_hidden_notification_row :
                            R.layout.layout_notification_row,
                    parent, false);
            int padding = (int) getResources().getDimension(R.dimen.spacing_large);
            view.setPadding(padding, padding, padding, padding);
            return viewType == TYPE_HIDDEN ? new HiddenHolder(view) : new MessageHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder.getItemViewType() == TYPE_MESSAGE) {
                MessageHolder messageHolder = (MessageHolder) holder;
                Message message = messages.get(position);
                messageHolder.iconView.setImageResource(message.type.getIconResource().getDrawableResId());
                messageHolder.iconView.setContentDescription(getString(message.type.getIconResource().getStringResId()));
                if (message.media != null) {
                    IconResource iconResource = message.media.getIconResource();
                    messageHolder.mediaIconView.setImageResource(iconResource.getDrawableResId());
                    messageHolder.mediaIconView.setContentDescription(getString(iconResource.getStringResId()));
                    messageHolder.mediaIconView.setVisibility(View.VISIBLE);
                } else {
                    messageHolder.mediaIconView.setVisibility(View.GONE);
                }
                messageHolder.titleView.setText(message.title);
                messageHolder.descriptionView.setText(message.description);
                messageHolder.descriptionView.setVisibility(message.description != null ? View.VISIBLE : View.GONE);
                messageHolder.timeView.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(message.timestamp));
                messageHolder.textView.setText(message.text);
                messageHolder.textView.setVisibility(message.text != null ? View.VISIBLE : View.GONE);
                messageHolder.mediaView.setImageBitmap(null);
                messageHolder.mediaLayout.setVisibility(View.GONE);
                messageHolder.layout.setOnClickListener((view) -> AndroidUtils.openUri(NotificationListActivity.this, message.url));
                messageHolder.expandButton.setTag(false);
                messageHolder.expandButton.setOnClickListener((view) ->
                        setExpanded(messageHolder, message, position, !expanded[position]));
                setExpanded(messageHolder, message, position, expanded[position]);
            }
        }

        private void setExpanded(MessageHolder holder, Message message, int position, boolean expanded) {
            NotificationListActivity.this.expanded[position] = expanded;
            if (expanded) {
                holder.expandButton.setImageResource(R.drawable.ic_collapse);
                holder.expandButton.setContentDescription(getString(R.string.collapse));
                holder.textView.setMaxLines(Integer.MAX_VALUE);
                holder.textView.setEllipsize(null);
                if (message.media != null && showMedia) {
                    AndroidUtils.downloadMedia(message.media, 1024, 512, result -> {
                        if (holder.getAdapterPosition() == position) {
                            if (result.getData() != null) {
                                holder.mediaView.setImageBitmap(result.getData());
                                holder.mediaView.setVisibility(View.VISIBLE);
                                IconResource iconResource = message.media.getIconResource();
                                holder.mediaImageIconView.setImageResource(iconResource.getDrawableResId());
                                holder.mediaImageIconView.setContentDescription(getString(iconResource.getStringResId()));
                            } else {
                                holder.mediaView.setVisibility(View.GONE);
                                holder.mediaImageIconView.setImageResource(R.drawable.ic_error);
                                holder.mediaImageIconView.setContentDescription(getString(R.string.error));
                            }
                            holder.mediaLayout.setVisibility(View.VISIBLE);
                        }
                    });
                }
            } else {
                holder.expandButton.setImageResource(R.drawable.ic_expand);
                holder.expandButton.setContentDescription(getString(R.string.expand));
                holder.textView.setMaxLines(2);
                holder.textView.setEllipsize(TextUtils.TruncateAt.END);
                holder.mediaLayout.setVisibility(View.GONE);
            }
        }

        private class MessageHolder extends RecyclerView.ViewHolder {

            View layout;
            ImageButton expandButton;
            ImageView iconView;
            ImageView mediaIconView;
            TextView titleView;
            TextView timeView;
            TextView descriptionView;
            TextView textView;
            View mediaLayout;
            ImageView mediaView;
            ImageView mediaImageIconView;

            public MessageHolder(@NonNull View itemView) {
                super(itemView);

                layout = itemView;
                expandButton = itemView.findViewById(R.id.notification_row_expand);
                iconView = itemView.findViewById(R.id.notification_row_icon);
                mediaIconView = itemView.findViewById(R.id.notification_row_media_icon);
                titleView = itemView.findViewById(R.id.notification_row_title);
                timeView = itemView.findViewById(R.id.notification_row_time);
                descriptionView = itemView.findViewById(R.id.notification_row_description);
                textView = itemView.findViewById(R.id.notification_row_text);
                mediaLayout = itemView.findViewById(R.id.notification_row_media_layout);
                mediaView = itemView.findViewById(R.id.notification_row_media_image);
                mediaImageIconView = itemView.findViewById(R.id.notification_row_media_image_icon);
            }

        }

        private class HiddenHolder extends RecyclerView.ViewHolder {

            public HiddenHolder(@NonNull View itemView) {
                super(itemView);
            }

        }

    }

}
