package com.example.raelee.moodyj;

// 스트리밍 관련 리스트
class StreamerList {

    public String userId;
    public String thumbnail;


    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail){
        this.thumbnail = thumbnail;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }


    @Override
    public String toString(){
        return "StreamerList {" +
                "userId='" + userId + '\'' +
                "thumbnail='" + thumbnail + '\'' +
                '}';
    }

    public StreamerList(String UserId, String Thumbnail){
        userId = UserId;
        thumbnail = Thumbnail;

    }

    public StreamerList(){

    }

}
