package com.example.user.musicplayerlib;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.PlaybackStateCompat;


public class PlayerNotificationManager {

    private MusicPlayerService service;

    private static final String CHANNEL_ID = "ServiceChannel";


    PlayerNotificationManager(MusicPlayerService service) {

        this.service = service;
    }

    @SuppressLint("ResourceAsColor")
    void startNotify(PlaybackStateCompat build) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(service, CHANNEL_ID);

        int icon;
        String play_pause;
        if (build.getState() == PlaybackStateCompat.STATE_PLAYING) {
            icon = R.drawable.exo_controls_pause;
            play_pause = service.getString(R.string.pause);
        } else {
            icon = R.drawable.exo_controls_play;
            play_pause = service.getString(R.string.play);
        }

        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                icon, play_pause,
                MediaButtonReceiver.buildMediaButtonPendingIntent(service,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE));

        Intent notificatonIntent = new Intent(service, PlayerActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity
                (service, 0, notificatonIntent, 0);

        builder
                .setContentTitle("Music Player")
                .setContentIntent(contentPendingIntent)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_music)
                .setColor(R.color.white)
                .addAction(playPauseAction)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_STOP));

        if (build.getState() == PlaybackStateCompat.STATE_PLAYING)
            service.startForeground(10, builder.build());
        else {
            service.startForeground(10, builder.build());
            service.stopForeground(false);
        }
    }

    public void cancelNotify() {
        service.stopForeground(true);
    }


}
