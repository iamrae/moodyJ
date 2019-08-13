package com.example.raelee.moodyj;

import java.io.Serializable;

public class SortedMusicList implements Serializable {
    private String songId, albumId, title, artist, duration, displayName, data;

    public String getSongId(){
        return songId;
    }

    public void setSongId(String songId){
        this.songId = songId;
    }

    public String getAlbumId(){
        return albumId;
    }

    public void setAlbumId(String albumId){
        this.albumId = albumId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist){
        this.artist = artist;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration){
        this.duration = duration;
    }

    public String getData() {
        return data;
    }

    public void setData(String data){
        this.data = data;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return "MusicList{" +
                "songId='" + songId + '\'' +
                ", albumId='" + albumId + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", duration='" + duration + '\'' +
                ", data='" + data + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }

}