package com.example.user.musicplayerlib;

import android.net.Uri;

public class Song {

    private long id;
    private Uri uri;
    private String title;
    private String duration;
    private String artist;
    private String album;
    private String genre;
    private String AlbumArt;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    void setUri(Uri uri) {
        this.uri = uri;
    }

    Uri getUri() {
        return uri;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    void setDuration(String duration) {
        this.duration = duration;
    }

    String getDuration() {
        return duration;
    }

    void setArtist(String artist) {
        this.artist = artist;
    }

    String getArtist() {
        return artist;
    }

    void setAlbum(String album) {
        this.album = album;
    }

    String getAlbum() {
        return album;
    }

    void setGenre(String genre) {
        this.genre = genre;
    }

    String getGenre() {
        return genre;
    }

    void setAlbumArt(String albumArt) {
        AlbumArt = albumArt;
    }

    String getAlbumArt() {
        return AlbumArt;
    }
}
