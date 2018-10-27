package com.example.user.musicplayerlib;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.ArrayList;
import java.util.List;

class MusicPlayerManager {

    private SimpleExoPlayer player;
    private Context context;
    private PlayerView playerView;
    private AudioAttributes audioAttributes;

    private long playBackPosition;
    private int currentWindow;
    private boolean playWhenReady;


    MusicPlayerManager(Context context, PlayerView playerView) {

        this.context = context;
        this.playerView = playerView;
        this.audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build();
    }

    SimpleExoPlayer getPlayer() {
        return player;
    }

    void initializePlayer() {

        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance(
                    context,
                    new DefaultRenderersFactory(context),
                    new DefaultTrackSelector(),
                    new DefaultLoadControl()
            );

            // handling audio focus todo need to test
            player.setAudioAttributes(audioAttributes, true);

            playerView.setPlayer(player);
            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentWindow, playBackPosition);
        }


        // todo use for instead
        Uri uri1 = Uri.parse(context.getString(R.string.girls_like_u_mp3));
        Uri uri2 = Uri.parse(context.getString(R.string.nem_mp3));

        List<Uri> uris = new ArrayList<>();
        uris.add(uri1);
        uris.add(uri2);

        //MediaSource mediaSource = buildMediaSource(uri2);
        MediaSource mediaSource = buildMediaSource(uris);
        player.prepare(mediaSource, true, false);
    }

    void releasePlayer() {

        if (player != null) {
            playBackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    // one music
    private MediaSource buildMediaSource(Uri musicUri) {

        CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(
                context,
                100 * 1024 * 1024,
                5 * 1024 * 1024);

        if (musicUri.getPathSegments().contains("mp3") || musicUri.getPathSegments().contains("m3u8")) {
            ExtractorMediaSource mediaSource = new ExtractorMediaSource.Factory(cacheDataSourceFactory)
                    //new DefaultHttpDataSourceFactory("exoplayer-codelab"))
                    .createMediaSource(musicUri);

            return new LoopingMediaSource(mediaSource);
        }
        return null;
    }

    // list of musics
    private MediaSource buildMediaSource(List<Uri> musicUris) {

        CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(
                context,
                100 * 1024 * 1024,
                5 * 1024 * 1024);
        //for (int i = 0; i < musics.size(); i++) {

        ExtractorMediaSource music1 =
                new ExtractorMediaSource.Factory(cacheDataSourceFactory)
//                        new DefaultHttpDataSourceFactory("exoplayer-codelab"))
                        .createMediaSource(musicUris.get(0));

        ExtractorMediaSource music2 =
                new ExtractorMediaSource.Factory(cacheDataSourceFactory)
//                        new DefaultHttpDataSourceFactory("exoplayer-codelab"))
                        .createMediaSource(musicUris.get(1));
        //}

        return new LoopingMediaSource(new ConcatenatingMediaSource(music1, music2));
    }


}
