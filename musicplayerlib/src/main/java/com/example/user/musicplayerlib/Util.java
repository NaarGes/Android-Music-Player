package com.example.user.musicplayerlib;

class Util {

    static final String CHANNEL_ID = "ServiceChannel";

    static String musicDuration(String timeLong) {

        long timeInMilliSec = Long.parseLong(timeLong);
        long duration = timeInMilliSec / 1000;
        long hours = duration / 3600;
        long minutes = (duration - hours * 3600) / 60;
        long seconds = duration - (hours * 3600 + minutes * 60);

        String time;
        if (hours == 0)
            time = "";
        else if (hours < 10)
            time = "0" + hours + ":";
        else time = hours + ":";

        if (minutes < 10)
            time += "0" + minutes;
        else time += minutes;
        time += ":";

        if (seconds < 10)
            time += "0" + seconds;
        else time += seconds;

        return time;
    }
}
