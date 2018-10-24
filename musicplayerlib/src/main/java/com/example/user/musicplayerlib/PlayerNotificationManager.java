package com.example.user.musicplayerlib;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;


public class PlayerNotificationManager {

    private MusicPlayerService service;
    private String appName;
    private Resources resources;

    private final int REQUEST_CODE_PAUSE = 1;
    private final int REQUEST_CODE_PLAY = 2;
    private final int REQUEST_CODE_STOP = 3;
    private final int NOTIFICATION_ID = 555;


    PlayerNotificationManager(MusicPlayerService service) {

        this.service = service;
        this.resources = service.getResources();
        this.appName = String.valueOf(R.string.app_name);
    }

    public PendingIntent createAction(String action, int requestCode) {

        Intent intent = new Intent(service, MusicPlayerService.class);
        intent.setAction(action);
        return PendingIntent.getService(service, requestCode, intent, 0);
    }

    public void startNotify(String playbackStatus) {

       /* int iccon = R.drawable.exo_icon_pause;
        PendingIntent playPauseAction = createAction(MusicPlayerService.ACTION_PAUSE, REQUEST_CODE_PAUSE);

        if (playbackStatus == "paused") {

            iccon = R.drawable.exo_icon_play;
            playPauseAction = createAction(MusicPlayerService.ACTION_PLAY, REQUEST_CODE_PLAY);
        }

        PendingIntent stopAction = createAction(MusicPlayerService.ACTION_STOP, REQUEST_CODE_STOP);

        Intent intent = new Intent(service, PlayerActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(service, 0, intent, 0);

        NotificationManagerCompat.from(service).cancel(NOTIFICATION_ID);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(service, String.valueOf(NOTIFICATION_ID))
                .setSmallIcon(R.drawable.exo_edit_mode_logo)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setContentTitle(appName)
                .setContentText("test video service")
                .setContentIntent(pendingIntent)
                .addAction(iccon, "pause", playPauseAction)
                .addAction(R.drawable.exo_icon_stop, "stop", stopAction);

        service.startForeground(NOTIFICATION_ID, builder.build());*/

    }

    public void cancelNotify() {
        service.stopForeground(true);
    }


}
