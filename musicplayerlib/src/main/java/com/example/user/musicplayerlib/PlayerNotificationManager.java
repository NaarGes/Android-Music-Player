package com.example.user.musicplayerlib;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.HashMap;

import static com.example.user.musicplayerlib.Util.CHANNEL_ID;
import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;


class PlayerNotificationManager {

    private MusicPlayerService service;
    private Song song;

    PlayerNotificationManager(MusicPlayerService service) {

        this.service = service;
        song = new Song();
    }

    // fixme call it when track changes
    // fixme update notification ui after running thread
    void updateNotification(Uri musicUri) {

        Thread thread = new Thread(() -> {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(String.valueOf(musicUri), new HashMap<>());

            song.setUri(musicUri);
            song.setTitle(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            song.setArtist(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            song.setAlbum(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            song.setDuration(Util.musicDuration(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
            song.setGenre(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));

            byte[] art = retriever.getEmbeddedPicture();

            if(art != null)
                song.setAlbumArt(BitmapFactory.decodeByteArray(art, 0, art.length));
            else
                song.setAlbumArt(BitmapFactory.decodeResource(service.getResources(), R.drawable.ic_music));

            Log.e(TAG, "updateNotification: " + song.getAlbumArt());
        });
        thread.start();

    }

    @SuppressLint("ResourceAsColor")
    void startNotify(PlaybackStateCompat build) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(service, CHANNEL_ID);
        Drawable drawable = new BitmapDrawable(service.getResources(), song.getAlbumArt());

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
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setShowCancelButton(true)
                        .setMediaSession(service.getSession().getSessionToken()))
                .setColorized(true)
                .setContentTitle(song.getTitle())
                .setContentText(song.getArtist())
                .setContentIntent(contentPendingIntent)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .addAction(prevAction)
                .addAction(playPauseAction)
                .addAction(nextAction)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_STOP));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder.setLargeIcon(song.getAlbumArt());
        else
            builder.setSmallIcon(R.drawable.ic_music);

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
