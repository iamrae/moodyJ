package com.example.raelee.moodyj;

import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// 단말기에 있는 음악 관련 정보 받아오기
public class MusicList implements Serializable {
    private String songId;
    private String albumId;
    private String title;
    private String artist;
    private String duration;
    private String displayName;
    private String data;


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
