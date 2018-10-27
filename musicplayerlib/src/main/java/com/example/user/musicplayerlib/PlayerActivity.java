package com.example.user.musicplayerlib;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

public class PlayerActivity extends AppCompatActivity implements ServiceConnection {

    //private MusicPlayerFragment musicPlayerFragment;
    private boolean bound;
    private PlayerView playerView;
    private MusicPlayerService musicPlayerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playerView = findViewById(R.id.exo_player);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MusicPlayerService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(this);
            bound = false;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        Log.d("Player Activity", "onServiceConnected");
        MusicPlayerService.MyBinder binder = (MusicPlayerService.MyBinder) service;
        musicPlayerService = binder.getService();
        playerView.setPlayer(musicPlayerService.getExoPlayer());
        if (musicPlayerService.getMediaUri() == null) {
            musicPlayerService.buildMediaSource(Uri.parse(String.valueOf(R.string.girls_like_u_mp3)));
        }
        bound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        bound = false;
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUiFullScreen() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUiFullScreen();
        }
        else {
            hideSystemUi();
        }
    }
}
