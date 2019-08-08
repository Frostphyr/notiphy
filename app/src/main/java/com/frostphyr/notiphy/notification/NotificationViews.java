package com.frostphyr.notiphy.notification;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.frostphyr.notiphy.NsfwContent;

public abstract class NotificationViews {

    private OnUpdateListener updateListener;
    private Context context;
    private NsfwContent nsfwContent;
    private boolean showMedia;
    private int id;

    public NotificationViews(Context context, NsfwContent nsfwContent, boolean showMedia, int id) {
        this.context = context;
        this.nsfwContent = nsfwContent;
        this.showMedia = showMedia;
        this.id = id;
    }

    public Context getContext() {
        return context;
    }

    public NsfwContent getNsfwContent() {
        return nsfwContent;
    }

    public boolean isShowMedia() {
        return showMedia;
    }

    public int getId() {
        return id;
    }

    public void setOnUpdateListener(OnUpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    protected void onUpdate() {
        if (updateListener != null) {
            updateListener.onUpdate(this);
        }
    }

    public abstract long getWhen();

    public abstract RemoteViews getSmallView();

    public abstract RemoteViews getBigView();

    public abstract Intent getIntent();

    public interface OnUpdateListener {

        void onUpdate(NotificationViews views);

    }

}
