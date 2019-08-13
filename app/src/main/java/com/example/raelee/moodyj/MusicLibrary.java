package com.example.raelee.moodyj;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaExtractor;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class MusicLibrary {

    // 음악 재생목록 관련 클래스
    // mediaItem, (QueueItem), Metadata, Notification Builder 등을 포함

    private static final TreeMap<String, MediaMetadataCompat> music = new TreeMap<>();
    private static final HashMap<String, Integer> albumRes = new HashMap<>();
    private static final HashMap<String, String> musicFileName = new HashMap<>();
    private static final HashMap<String, String> albumNo = new HashMap<>();
    private static final HashMap<String, String> albumArtUri = new HashMap<>();

    //ArrayList<MusicList> list = MainActivity.list;


    public static String getRoot() {
        return "root";
    }


    private static String getAlbumArtUri(String mediaId) {
//        return albumArtUri.containsKey(mediaId)? albumArtUri.get(mediaId) : null;
        /*return ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                BuildConfig.APPLICATION_ID + "/drawable/" + albumArtResName;*/
        return "content://media/external/audio/albumart/" + getAlbumNo(mediaId);

    }

    public static String getMusicFilename(String mediaId) {
        return musicFileName.containsKey(mediaId) ? musicFileName.get(mediaId) : null;
    }


    private static String getAlbumNo(String mediaId){
        return albumNo.containsKey(mediaId)? albumNo.get(mediaId) : null;
    }


    public static Bitmap getAlbumBitmap(Context context, String mediaId) {
        Log.e("getAlbumBitmap : ", ""+ MusicLibrary.getAlbumArtUri(mediaId));
        return BitmapFactory.decodeFile(MusicLibrary.getCoverArtPath(MusicLibrary.getAlbumNo(mediaId), context));
        //return BitmapFactory.decodeResource(context.getResources(), MusicLibrary.getAlbumRes(mediaId));
        //return BitmapFactory.decodeFile(MusicLibrary.getAlbumArtUri(mediaId));
    }


    private static String getCoverArtPath(String albumId, Context context){

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + " = ?",
                new String[]{albumId},
                null
        );
        Log.e("getCoverArtPath ", "" + cursor);


        boolean queryResult = cursor.moveToFirst();
        String result = null;
        if(queryResult) {
            result = cursor.getString(0);
        }
        cursor.close();
        return result;
    }

    public static List<MediaBrowserCompat.MediaItem> getMediaItems() {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        for (MediaMetadataCompat metadata : music.values()) {
            result.add(
                    new MediaBrowserCompat.MediaItem(metadata.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
            );
        }
        return result;
    }

/*    public static List<MediaBrowserCompat.MediaItem> getSingleMediaItem() {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        for (MediaMetadataCompat metadata : music.values()) {
            result.add(
                    new MediaBrowserCompat.MediaItem(metadata.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
            );
        }
        return result;
    }*/

    public static MediaMetadataCompat getMetadata(Context context, String mediaId) {
        MediaMetadataCompat metadataWithoutBitmap = music.get(mediaId);
        Bitmap albumArt = getAlbumBitmap(context, mediaId);
        Log.e(TAG, "getMetadata + Bitmap albumArt = " + albumArt);

        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        for (String key:
                new String[]{
                        MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
                        MediaMetadataCompat.METADATA_KEY_ALBUM,
                        MediaMetadataCompat.METADATA_KEY_ARTIST,
                       // MediaMetadataCompat.METADATA_KEY_GENRE,
                        MediaMetadataCompat.METADATA_KEY_TITLE
                }) {
            builder.putString(key, metadataWithoutBitmap.getString(key));
        }
        builder.putLong(
                MediaMetadataCompat.METADATA_KEY_DURATION,
                metadataWithoutBitmap.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt);
        return  builder.build();

    }

    public static void createMediaMetadataCompat(
            String mediaId,
            String title,
            String artist,
            String album,
            //String genre,
            long duration,
            TimeUnit durationUnit,
            String displayName,
            String data,
            //String albumArtResName,
            //int albumArtResId,
            String albumId) {
        music.put(
                mediaId,
                new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                            TimeUnit.MILLISECONDS.convert(duration, durationUnit))
                    //.putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, data)
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, data)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                    .build());
       // albumRes.put(mediaId, albumArtResId);
        albumNo.put(mediaId, album);
        musicFileName.put(mediaId, displayName);
        albumArtUri.put(mediaId, albumId);
    }

}
