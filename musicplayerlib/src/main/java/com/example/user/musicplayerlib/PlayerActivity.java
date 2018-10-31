package com.example.user.musicplayerlib;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.google.android.exoplayer2.ui.PlayerView;

public class PlayerActivity extends AppCompatActivity implements ServiceConnection {

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
        if (musicPlayerService.getPlayList() == null) {
            musicPlayerService.buildMediaSource(Uri.parse(String.valueOf(R.string.girls_like_u_mp3)));
        }
        bound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        bound = false;
    }
}
