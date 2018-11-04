package com.example.user.musicplayerlib;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;


import java.util.ArrayList;
import java.util.List;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.TAG;


public class MusicPlayerService extends Service implements Player.EventListener,
        AudioManager.OnAudioFocusChangeListener {

    private static final String LOG_TAG = MusicPlayerService.class.getSimpleName();

    private final IBinder binder = new MyBinder();
    private SimpleExoPlayer exoPlayer;
    private PlaybackStateCompat.Builder stateBuilder;
    private AudioAttributes audioAttributes;
    private boolean isBounded;

    private List<Uri> playListUri;

    private PlayerNotificationManager notificationManager;
    private MediaSessionCompat session;
    private BroadcastReceiver noisyReceiver;

    private Timeline.Window currentWindow;
    private static final long MAX_POSITION_FOR_SEEK_TO_PREVIOUS = 3000;

    @Override
    public void onCreate() {
        super.onCreate();

        playListUri = new ArrayList<>();
        currentWindow = new Timeline.Window();
        notificationManager = new PlayerNotificationManager(this);

        audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build();

        noisyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if( exoPlayer != null && exoPlayer.getPlayWhenReady() ) {
                    exoPlayer.setPlayWhenReady(false);
                }
            }
        };

        initializeMediaSession();
        initializePlayer();
        initNoisyReceiver();
    }

    // This method will take the Intent that is passed to the Service and send it to the MediaButtonReceiver class.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(LOG_TAG, "onStartCommand");
        MediaButtonReceiver.handleIntent(session, intent);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOG_TAG, "onBind");
        isBounded = true;
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        isBounded = true;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(LOG_TAG, "onUnbind");
        isBounded = false;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(this);
        unregisterReceiver(noisyReceiver);
        notificationManager.cancelNotify();
//        NotificationManagerCompat.from(this).cancel(1);
        releasePlayer();
        session.setActive(false);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }

    public class MyBinder extends Binder {
        MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    public SimpleExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    // one music
    public MediaSource buildMediaSource(Uri musicUri) {

        playListUri.add(musicUri);
        CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(
                this,
                100 * 1024 * 1024,
                5 * 1024 * 1024);

        if (musicUri.getPathSegments().contains("mp3") || musicUri.getPathSegments().contains("m3u8")) {
            ExtractorMediaSource mediaSource = createMediaSource(cacheDataSourceFactory, musicUri);

            return new LoopingMediaSource(mediaSource);
        }
        return null;
    }

    // looping list of musics
    public MediaSource buildMediaSource(List<Uri> musicUris) {

        playListUri.addAll(musicUris);
        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(
                this,
                100 * 1024 * 1024,
                5 * 1024 * 1024);

        for (int i=0; i<musicUris.size();i++) {
            concatenatingMediaSource.addMediaSource(
                    createMediaSource(cacheDataSourceFactory, musicUris.get(i)));
        }

        return concatenatingMediaSource;
    }

    private ExtractorMediaSource createMediaSource(CacheDataSourceFactory cacheDataSourceFactory, Uri uri) {

        return new ExtractorMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(uri);
    }

    private void initializeMediaSession() {

        // Create a MediaSessionCompat.
        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(),
                MediaButtonReceiver.class);

        session = new MediaSessionCompat(this, getClass().getSimpleName(),
                mediaButtonReceiver, null);

        // Enable callbacks from MediaButtons and TransportControls.
        session.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Do not let MediaButtons restart the player when the app is not visible.
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                mediaButtonIntent, 0);
        session.setMediaButtonReceiver(pendingIntent);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_STOP |
                        PlaybackStateCompat.ACTION_PLAY_PAUSE);

        session.setPlaybackState(stateBuilder.build());

        // MySessionCallback has methods that handle callbacks from a media controller.
        session.setCallback(new MySessionCallback());

        // Start the Media Session since the activity is active.
        session.setActive(true);
    }

    private void initializePlayer() {
        if (exoPlayer == null) {
            // Create an instance of the ExoPlayer.;
            exoPlayer = ExoPlayerFactory.newSimpleInstance(
                    this,
                    new DefaultRenderersFactory(this),
                    new DefaultTrackSelector(),
                    new DefaultLoadControl()
            );
            // handling audio focus
            exoPlayer.setAudioAttributes(audioAttributes, true);

            // add event listener
            exoPlayer.addListener(this);

            //MediaSource mediaSource = buildMediaSource(uri);
            MediaSource mediaSource = buildMediaSource(generatePlayListUri());
            exoPlayer.prepare(mediaSource, true, false);
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
        }
    }

    //Handles headphones coming unplugged. cannot be done through a manifest receiver
    private void initNoisyReceiver() {

        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(noisyReceiver, filter);
    }

    private void releasePlayer() {
        stopForeground(true);
        exoPlayer.stop();
        exoPlayer.release();
        exoPlayer = null;
    }

    // return playListUri
    public List<Uri> getPlayListUri() {
        return playListUri;
    }

    public MediaSessionCompat getSession() {
        return session;
    }

    // generate and return list of music uris
    public List<Uri> generatePlayListUri() {
        Uri uri1 = Uri.parse(getString(R.string.girls_like_u_mp3));
        Uri uri2 = Uri.parse(getString(R.string.nem_mp3));
        Uri uri3 = Uri.parse(getString(R.string.my_immortal));
        Uri uri4 = Uri.parse(getString(R.string.believer));
        Uri uri5 = Uri.parse(getString(R.string.in_the_end));

        List<Uri> uris = new ArrayList<>();
        uris.add(uri1);
        uris.add(uri2);
        uris.add(uri3);
        uris.add(uri4);
        uris.add(uri5);

        return uris;
    }
//playListUri.get(exoPlayer.getCurrentPeriodIndex())
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if ((playbackState == Player.STATE_READY) && playWhenReady) {
            Log.i(LOG_TAG, "onPlayerStateChanged palying");

            stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    exoPlayer.getCurrentPosition(), 1f);
        } else if ((playbackState == Player.STATE_READY)) {
            stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    exoPlayer.getCurrentPosition(), 1f);
            Log.i(LOG_TAG, "onPlayerStateChanged pause");

        } else if (playbackState == Player.STATE_ENDED) {
            stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, exoPlayer.getCurrentPosition(), 1f);
        }

        session.setPlaybackState(stateBuilder.build());

        Log.e(TAG, "onPlayerStateChanged: current period index "+exoPlayer.getCurrentPeriodIndex() );
        Log.e(TAG, "onPlayerStateChanged: current window index "+exoPlayer.getCurrentWindowIndex() );

        //notificationManager.updateNotification(playListUri.get(exoPlayer.getCurrentPeriodIndex()));
        notificationManager.startNotify(stateBuilder.build());
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        error.printStackTrace();
        exoPlayer.stop();
    }

    /**
     * Media Session Callbacks, where all external clients control the player.
     */
    private class MySessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPlay() {
            Log.i(LOG_TAG, "MySessionCallback Play");
            if (exoPlayer.getPlaybackState() == Player.STATE_ENDED)
                exoPlayer.seekTo(0);
            exoPlayer.setPlayWhenReady(true);
            session.setActive(true);
        }

        @Override
        public void onPause() {
            Log.i(LOG_TAG, "MySessionCallback Pause");
            exoPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onStop() {
            super.onStop();
            if (!isBounded)
                stopSelf();
            Log.i(LOG_TAG, "MySessionCallback stop");
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            next();
            Log.i(LOG_TAG, "MySessionCallback skip to next");
        }

        @Override
        public void onSkipToPrevious() {

            super.onSkipToPrevious();
            previous();
            Log.i(LOG_TAG, "MySessionCallback skip to next");
        }

        // handle volume changes using headphone, not working apple headphone
        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {

            KeyEvent keyEvent = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
                exoPlayer.setVolume(exoPlayer.getVolume() + 1);
                return true;
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN){
                exoPlayer.setVolume(exoPlayer.getVolume() - 1);
                return true;
            } else
                return super.onMediaButtonEvent(mediaButtonEvent);
        }
    }

    private void previous() {

        Timeline currentTimeline = exoPlayer.getCurrentTimeline();
        int currentWindowIndex = exoPlayer.getCurrentWindowIndex();

        if (currentTimeline.isEmpty())
            return;

        if (exoPlayer.getRepeatMode() == Player.REPEAT_MODE_ONE)
            exoPlayer.seekTo(0);
        else if (exoPlayer.getRepeatMode() == Player.REPEAT_MODE_ALL) {
            if (currentWindowIndex == 0)
                exoPlayer.seekTo(playListUri.size() - 1, C.TIME_UNSET);
            else
                exoPlayer.seekTo((currentWindowIndex - 1) % playListUri.size(), C.TIME_UNSET);
        } else {
            if (currentWindowIndex > 0 && (exoPlayer.getCurrentPosition() <= MAX_POSITION_FOR_SEEK_TO_PREVIOUS
                    || (currentWindow.isDynamic && !currentWindow.isSeekable)))
                exoPlayer.seekTo(currentWindowIndex - 1, C.TIME_UNSET);
            else
                exoPlayer.seekTo(0);
        }
    }

    private void next() {

        Timeline currentTimeline = exoPlayer.getCurrentTimeline();
        int currentWindowIndex = exoPlayer.getCurrentWindowIndex();

        if (currentTimeline.isEmpty())
            return;

        if (exoPlayer.getRepeatMode() == Player.REPEAT_MODE_ONE)
            exoPlayer.seekTo(0);
        else if (exoPlayer.getRepeatMode() == Player.REPEAT_MODE_ALL)
            exoPlayer.seekTo((currentWindowIndex + 1) % playListUri.size(), C.TIME_UNSET);
        else {// Player.REPEAT_MODE_OFF
            if (currentWindowIndex < currentTimeline.getWindowCount() - 1)
                exoPlayer.seekTo(currentWindowIndex + 1, C.TIME_UNSET);
            else if (currentTimeline.getWindow(currentWindowIndex, currentWindow, false).isDynamic)
                exoPlayer.seekTo(currentWindowIndex, C.TIME_UNSET);
        }
    }
}
