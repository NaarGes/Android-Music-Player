package com.example.user.musicplayerlib;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.PlaybackStateCompat;


class PlayerNotificationManager {

    private MusicPlayerService service;

    private static final String CHANNEL_ID = "ServiceChannel";


    PlayerNotificationManager(MusicPlayerService service) {

        this.service = service;
    }

    @SuppressLint("ResourceAsColor")
    void startNotify(PlaybackStateCompat build) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(service, CHANNEL_ID);

        int icon;
        if (build.getState() == PlaybackStateCompat.STATE_PLAYING)
            icon = R.drawable.exo_controls_pause;
        else
            icon = R.drawable.exo_controls_play;


        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                icon, "",
                MediaButtonReceiver.buildMediaButtonPendingIntent(service,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE));

        NotificationCompat.Action nextAction = new NotificationCompat.Action(
                R.drawable.exo_icon_next, "", //String.valueOf(R.string.next),
                MediaButtonReceiver.buildMediaButtonPendingIntent(service,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT));

        NotificationCompat.Action prevAction = new NotificationCompat.Action(
                R.drawable.exo_icon_previous, "", //String.valueOf(R.string.previous),
                MediaButtonReceiver.buildMediaButtonPendingIntent(service,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));

        Intent notificationIntent = new Intent(service, PlayerActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity
                (service, 0, notificationIntent, 0);

        builder
                .setContentTitle("Music Player")
                .setContentIntent(contentPendingIntent)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_music)
                .addAction(prevAction)
                .addAction(playPauseAction)
                .addAction(nextAction)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_STOP));

        if (build.getState() == PlaybackStateCompat.STATE_PLAYING)
            service.startForeground(10, builder.build());
        else {
            service.startForeground(10, builder.build());
            service.stopForeground(false);
        }
    }

    void cancelNotify() {
    }
}
