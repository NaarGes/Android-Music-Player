package com.example.user.musicplayerlib;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;

public class MusicPlayerWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.music_player_widget);

            PendingIntent playPauseAction
                    = MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY_PAUSE);
            PendingIntent nextAction
                    = MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
            PendingIntent previousAction
                    = MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);

            views.setOnClickPendingIntent(R.id.widget_play, playPauseAction);
            views.setOnClickPendingIntent(R.id.widget_next, nextAction);
            views.setOnClickPendingIntent(R.id.widget_previous, previousAction);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    // FIXME override onReceive to start service
    // https://stackoverflow.com/questions/17515353/start-stop-service-from-widget

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }
}