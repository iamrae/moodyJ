package com.example.raelee.moodyj;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class MyMusicListActivity extends Activity {

    private static final String TAG = "MyMusicListActivity";

    //ArrayList<MusicList> list = MainActivity.list;
    ArrayList<MusicList> metaList;
    MusicLibrary musicLibrary = MainActivity.musicLibrary;
    RecyclerView mRecyclerView;
    RecyclerViewAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    String title, artist, songId, albumId, position, duration;

    private MediaBrowserHelper mMediaBrowserHelper;

    private boolean mIsPlaying;
    private boolean loadedBefore;
    Context mContext;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 타이틀바 삭제
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // notification bar 삭제
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // title/notification bar 삭제 후 레이아웃 띄우기
        this.setContentView(R.layout.activity_my_music_list);

        // FontAwesome 서체를 사용하여 아이콘 배치
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);


        mMediaBrowserHelper = new MediaBrowserConnection(this);
        mMediaBrowserHelper.registerCallback(new MediaBrowserListener());

        mRecyclerView = findViewById(R.id.recycler_view);
        // mAdapter = new RecyclerViewAdapter(this, list);

        metaList = new ArrayList<>();
        loadedBefore = false;

    }

    @Override
    public void onStart() {
        super.onStart();
        mMediaBrowserHelper.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        mMediaBrowserHelper.onStop();
    }


    @Override
    protected void onResume() {
        super.onResume();

        /*mAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() { // 어댑터에 온아이템클릭리스너 생성
            @Override
            public void onItemClick(int position) {


*//*
                // 선택된 곡의 제목, 아티스트명, 곡의 ID 값
                title = list.get(position).getTitle();
                artist = list.get(position).getArtist();
                songId = list.get(position).getSongId();
                albumId = list.get(position).getAlbumId();
                duration = list.get(position).getDuration();*//*


                // 선택된 곡의 제목, 아티스트명, 곡의 ID 값
                title = metaList.get(position).getTitle();
                artist = metaList.get(position).getArtist();
                songId = metaList.get(position).getSongId();
                albumId = metaList.get(position).getAlbumId();
                //duration = metaList.get(position).getDuration();

                *//*
                long getUri = Long.parseLong(songId);
                Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, getUri);

                Log.e(TAG, "OnItemClickListener getUri: "+ getUri);
                Log.e(TAG, "OnItemClickListener trackUri: "+ trackUri);
                *//*


                // 재생목록에 있는 곡을 누르면, 뮤직플레이어 액티비티로 이동하여 해당 곡을 재생하게 하는 인텐트
                // 전달 값 : 노래 제목, 아티스트명, 곡 ID, ArrayList 에서의 위치값
                Intent onclick = new Intent(MyMusicListActivity.this, PlayerActivity.class);
                // onclick.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                onclick.putExtra("title", title);
                onclick.putExtra("artist", artist);
                onclick.putExtra("songID", songId);
                onclick.putExtra("position", position);
                //onclick.putExtra("duration", duration);
                Log.e(TAG, "SONGID 나와라" + title + " | " + songId + " | " + albumId + " | " + position);

                startActivity(onclick);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    mMediaBrowserHelper.getTransportControls().skipToQueueItem((long)position);
                    //mMediaBrowserHelper.getTransportControls().skipToQueueItem(Long.parseLong(songId));
                   // Log.e(TAG, "OnItemClickListener skipToQueueItem: "+ Long.parseLong(songId));
                    Log.e(TAG, "OnItemClickListener skipToQueueItem: "+ position);
                    mMediaBrowserHelper.getTransportControls().play();

                }

            }

        });
        */
    }


    /**
     * Customize the connection to our {@link android.support.v4.media.MediaBrowserServiceCompat}
     * and implement our app specific desires.
     */
    private class MediaBrowserConnection extends MediaBrowserHelper {
        private MediaBrowserConnection(Context context) {
            super(context, MusicService.class);
        }

        @Override
        protected void onConnected(@NonNull MediaControllerCompat mediaController) {
            Log.d(TAG, "MediaBrowserConnection : on Connected + mSeekBarAudio.setMediaController(controller)");
        }

        @Override
        protected void onChildrenLoaded(@NonNull String parentId,
                                        @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);

            Log.d(TAG, "MediaBrowserConnection : onChildrenLoaded");

            final MediaControllerCompat mediaController = getMediaController();

            if (!loadedBefore) {
                Log.e(TAG, "onChildrenLoaded: 매번 목록 로딩되는지 확인 " + loadedBefore);
                // Queue up all media items for this simple sample.
                int i = 0;
                for (final MediaBrowserCompat.MediaItem mediaItem : children) {
                    mediaController.addQueueItem(mediaItem.getDescription());
                    Log.e(TAG, "MediaBrowserConnection : onChildrenLoaded + addQueueItem "+ mediaItem.getDescription().getTitle() + "  mediaID : " + mediaItem.getMediaId() + " position : " + i);


                    MusicList mList = new MusicList();
                    String title = String.valueOf(mediaItem.getDescription().getTitle());
                    String artist = String.valueOf(mediaItem.getDescription().getSubtitle());
                    String albumId = String.valueOf(mediaItem.getDescription().getDescription());
/*
                // https://stackoverflow.com/questions/10631715/how-to-split-a-comma-separated-string : 콤마 기준으로 스트링 자르기
                String desc = String.valueOf(mediaItem.getDescription());
                String[] descriptions = desc.split("\\s*,\\s*"); // descriptions[0] : title, descriptions[1] : artist, descriptions[2] : albumID
*/

                    mList.setTitle(title);
                    mList.setArtist(artist);
                    mList.setAlbumId(albumId);
                    mList.setSongId(mediaItem.getMediaId());
                    metaList.add(mList);
                    i++;
                }
                loadedBefore = true;
                Log.e(TAG, "onChildrenLoaded: 매번 목록 로딩되는지 확인 " + loadedBefore);
            }

            mediaController.getQueue();
            // Call prepare now so pressing play just works.
            mediaController.getTransportControls().prepare();
            Log.e(TAG, "MediaBrowserConnection : onChildrenLoaded _ getMediaController.getTransportControls.prepare()");

            //mAdapter = new RecyclerViewAdapter(mContext, metaList);
            mAdapter = new RecyclerViewAdapter(MyMusicListActivity.this, metaList);
            mRecyclerView.setAdapter(mAdapter);
            //mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

            mRecyclerView.setLayoutManager(new LinearLayoutManager(MyMusicListActivity.this));

            mAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() { // 어댑터에 온아이템클릭리스너 생성
                @Override
                public void onItemClick(int position) {

                    // 선택된 곡의 제목, 아티스트명, 곡의 ID 값
                    title = metaList.get(position).getTitle();
                    artist = metaList.get(position).getArtist();
                    songId = metaList.get(position).getSongId();
                    albumId = metaList.get(position).getAlbumId();
                    //duration = metaList.get(position).getDuration();


                    // 재생목록에 있는 곡을 누르면, 뮤직플레이어 액티비티로 이동하여 해당 곡을 재생하게 하는 인텐트
                    // 전달 값 : 노래 제목, 아티스트명, 곡 ID, ArrayList 에서의 위치값
                    Intent onclick = new Intent(MyMusicListActivity.this, PlayerActivity.class);
                    // onclick.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    onclick.putExtra("title", title);
                    onclick.putExtra("artist", artist);
                    onclick.putExtra("songID", songId);
                    onclick.putExtra("position", position);
                    //onclick.putExtra("duration", duration);

                    startActivity(onclick);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        mMediaBrowserHelper.getTransportControls().skipToQueueItem((long)position);
                        //mMediaBrowserHelper.getTransportControls().skipToQueueItem(Long.parseLong(songId));
                        Log.e(TAG, "OnItemClickListener skipToQueueItem: "+ position);
                        mMediaBrowserHelper.getTransportControls().play();

                    }

                }

            });
        }
    }

    /**
     * Implementation of the {@link MediaControllerCompat.Callback} methods we're interested in.
     * <p>
     * Here would also be where one could override
     * {@code onQueueChanged(List<MediaSessionCompat.QueueItem> queue)} to get informed when items
     * are added or removed from the queue. We don't do this here in order to keep the UI
     * simple.
     */
    private class MediaBrowserListener extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
            mIsPlaying = playbackState != null &&
                    playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat mediaMetadata) {
            if (mediaMetadata == null) {
                return;
            }
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
        }
    }

}

