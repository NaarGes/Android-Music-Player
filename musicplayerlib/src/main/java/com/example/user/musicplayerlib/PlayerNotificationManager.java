package com.example.user.musicplayerlib;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.HashMap;

import static com.example.user.musicplayerlib.Util.CHANNEL_ID;
import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;

// TODO notification image
// TODO handle widget changes here (play/pause icon - song details)
class PlayerNotificationManager {

    private MusicPlayerService service;
    private Song song;
    private NotificationCompat.Builder builder;

    PlayerNotificationManager(MusicPlayerService service) {

        this.service = service;
        song = new Song();
    }

    // FIXME call it when track changes
    // FIXME update notification ui after running thread
    void updateNotification(Uri musicUri) {

        Log.e(TAG, "updateNotification: " + musicUri);
        // kill all other async tasks and run new one (Clicking multiple times on next or prev)

        new UpdateNotification().execute(musicUri);
    }

    @SuppressLint("ResourceAsColor")
    void startNotify(PlaybackStateCompat build) {

        builder = new NotificationCompat.Builder(service, CHANNEL_ID);

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
                (service, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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

    private class UpdateNotification extends AsyncTask<Uri, Void, Song> {


        @Override
        protected Song doInBackground(Uri... uris) {

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(String.valueOf(uris[0]), new HashMap<>());

            song.setUri(uris[0]);
            song.setTitle(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            song.setArtist(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            song.setAlbum(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            song.setDuration(Util.musicDuration(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
            song.setGenre(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));

            byte[] art = retriever.getEmbeddedPicture();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                song.setAlbumArt(BitmapFactory.decodeByteArray(art, 0, art.length));
            else
                song.setAlbumArt(BitmapFactory.decodeResource(service.getResources(), R.drawable.ic_music));

            Log.e(TAG, "updateNotification: " + song.getAlbumArt());

            return song;
        }

        @Override
        protected void onPostExecute(Song song) {
            super.onPostExecute(song);

            Log.e(TAG, "onPostExecute: "+song.getTitle()+ " "+ song.getArtist() + song.getAlbumArt() );

            // fixme builder should be new or cleaned
            builder.setContentTitle(song.getTitle())
                    .setContentText(song.getArtist());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                builder.setLargeIcon(song.getAlbumArt());
            else
                builder.setSmallIcon(R.drawable.ic_music);
            NotificationManager notificationManagerCompat =
                    (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManagerCompat.notify(10, builder.build());
        }
    }
}
