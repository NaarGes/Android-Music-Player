package com.example.user.musicplayerlib;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

public class MusicPlayerService extends Service implements Player.EventListener
        /*, AudioManager.OnAudioFocusChangeListener*/ {

    private String status;
    private PlayerNotificationManager notificationManager;
    private AudioAttributes audioAttributes;
    private SimpleExoPlayer player;
    private MediaSessionCompat session;
    private MediaControllerCompat.TransportControls transportControll;


    @Override
    public void onCreate() {
        super.onCreate();

        //notificationManager = new PlayerNotificationManager(this);

        session = new MediaSessionCompat(this, getClass().getSimpleName());
        transportControll = session.getController().getTransportControls();
        session.setActive(true);
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
        | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        status = "idle";

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        switch (playbackState) {

            case Player.STATE_BUFFERING:
                status = "loading"; // PlaybackStatus.LOADING;
                break;

            case Player.STATE_READY:
                status = playWhenReady ? "playing" : "paused"; //
                break;

            case Player.STATE_ENDED:
                status = "stopped"; // PlaybackStatus.STOPPED;
                break;

            case Player.STATE_IDLE:
                status = "idle"; // PlaybackStatus.IDLE;
                break;

            default:
                status = "idle";
        }
    }
}
