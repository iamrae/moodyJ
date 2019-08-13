package com.example.raelee.moodyj;

public class RecommendedLists {

    // 추천 목록 썸네일에 들어갈 항목들
    public String title;
    public String button;
    public String artist;
    public String fileName;
    public String albumArt;

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getButton() {
        return button;
    }

    public String getFileName() {
        return fileName;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setButton(String button) {
        this.button = button;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    @Override
    public String toString(){
        // arraylist 호출될때 리턴하는 내용 (제목 + 아티스트 + 파일명)
        return "RecommendedList {" +
                "title='" + title + '\'' +
                "artist='" + artist + '\'' +
                "fileName='" + fileName + '\'' +
                '}';
    }

    public RecommendedLists(){

    }

    public RecommendedLists(String title, String artist, String fileName){
        title = title;
        artist = artist;
        fileName = fileName;
    }

}
