package com.example.raelee.moodyj;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = "PlayerActivity";


    String title, artist, songId, durationSt;
    TextView textTitle, textArtist, seekStart, seekEnd;
    ImageView albumArt;
    TextView playIng, playPause, playPrev, playNext;
    TextView playShuffle, playRepeat;

    private final List<MediaControllerCompat.Callback> mCallbackList = new ArrayList<>();


    //private ProgressUpdate progressUpdate;
    public long position; // 재생곡의 리스트에서의 위치값


    private MediaSeekBar mSeekBarAudio;
    private MediaBrowserHelper mMediaBrowserHelper;
    private boolean mIsPlaying;
    private boolean mRepeating, mShuffling; // 랜덤 재생, 반복 재생 여부 저장하는 변수

    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 타이틀바 삭제
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // notification bar 삭제
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // title/notification bar 삭제 후 레이아웃 띄우기
        this.setContentView(R.layout.activity_player);

        // FontAwesome 서체를 사용하여 아이콘 배치
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);

        // 재생 컨트롤 버튼
        playIng = findViewById(R.id.playIng);
        playPause = findViewById(R.id.playPause);
        playPrev = findViewById(R.id.playPrev);
        playNext = findViewById(R.id.playNext);
        playShuffle = findViewById(R.id.playShuffle);
        playRepeat = findViewById(R.id.playRepeat);


        // 곡 정보
        albumArt = findViewById(R.id.view_albumArt);
        textTitle = findViewById(R.id.view_title);
        textArtist = findViewById(R.id.view_artist);
        mSeekBarAudio = findViewById(R.id.seekMusic);
        seekStart = findViewById(R.id.seekStart);
        seekEnd = findViewById(R.id.seekEnd);


        // 미디어브라우저 생성, 서비스와 연결
        mMediaBrowserHelper = new MediaBrowserConnection(this);
        mMediaBrowserHelper.registerCallback(new MediaBrowserListener());
        Log.e(TAG, "OnCreate 끝");

        if (playPause.getVisibility() == View.INVISIBLE){
            playPause.setVisibility(View.VISIBLE);
            playIng.setVisibility(View.INVISIBLE);
        }

        // 재생 관련 설정 저장, 공유하기 위한 SharedPreference
        preferences = getApplicationContext().getSharedPreferences("PlayerPref", 0); // 0 - for private mode

        if(preferences.contains("shuffle")) {
            mShuffling = preferences.getBoolean("shuffle", mShuffling);
            if (mShuffling) {
                playShuffle.setTextColor(Color.YELLOW);
            }

        }
        if (preferences.contains("repeat")){
            mRepeating = preferences.getBoolean("repeat", mRepeating);
            if (mRepeating) {
                playRepeat.setTextColor(Color.YELLOW);
            }
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        mMediaBrowserHelper.onStart();

        // 재생 컨트롤 버튼에 온클릭 리스너 연결
        final ClickListener clickListener = new ClickListener();
        findViewById(R.id.playIng).setOnClickListener(clickListener);
        findViewById(R.id.playPause).setOnClickListener(clickListener);
        findViewById(R.id.playPrev).setOnClickListener(clickListener);
        findViewById(R.id.playNext).setOnClickListener(clickListener);
        findViewById(R.id.playShuffle).setOnClickListener(clickListener);
        findViewById(R.id.playRepeat).setOnClickListener(clickListener);


    }


    protected void onStop(){
        super.onStop();
        mSeekBarAudio.disconnectController();
        mMediaBrowserHelper.onStop();
        Log.e(TAG, "onStop : mMediaBrowserCompat 연결종료");
        savePreferences();
    }

    protected void onDestroy(){
        super.onDestroy();
        clearPreferences();
        mMediaBrowserHelper.onStop();
    }


    /**
     * Convenience class to collect the click listeners together.
     * <p>
     * In a larger app it's better to split the listeners out or to use your favorite
     * library.
     */
    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.playPrev:
                    mMediaBrowserHelper.getTransportControls().skipToPrevious();

                    if (playPause.getVisibility() == View.INVISIBLE) {
                        playPause.setVisibility(View.VISIBLE);
                        playIng.setVisibility(View.INVISIBLE);
                    }

                    break;
                case R.id.playPause:
                    mMediaBrowserHelper.getTransportControls().pause();

                    if (playIng.getVisibility() == View.INVISIBLE) {
                        playIng.setVisibility(View.VISIBLE);
                        playPause.setVisibility(View.INVISIBLE);
                    }

                    break;
                case R.id.playIng:
                    mMediaBrowserHelper.getTransportControls().play();

                    if (playPause.getVisibility() == View.INVISIBLE) {
                        playPause.setVisibility(View.VISIBLE);
                        playIng.setVisibility(View.INVISIBLE);
                    }

                    break;
                case R.id.playNext:
                    mMediaBrowserHelper.getTransportControls().skipToNext();

                    if (playPause.getVisibility() == View.INVISIBLE) {
                        playPause.setVisibility(View.VISIBLE);
                        playIng.setVisibility(View.INVISIBLE);
                    }

                    break;
                case R.id.playShuffle:

                    if (!mShuffling) {
                        Log.e(TAG, "Shuffle ON");
                        mMediaBrowserHelper.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
                        playShuffle.setTextColor(Color.YELLOW);
                        mShuffling = true;

                    } else {
                        Log.e(TAG, "Shuffle OFF");
                        mMediaBrowserHelper.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
                        playShuffle.setTextColor(Color.WHITE);
                        mShuffling = false;
                    }

                    break;

                case R.id.playRepeat:

                    if (!mRepeating) {
                        Log.e(TAG, "Repetition ON");
                        mMediaBrowserHelper.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
                        playRepeat.setTextColor(Color.YELLOW);
                        mRepeating = true;

                    } else {
                        Log.e(TAG, "Repetition OFF");
                        mMediaBrowserHelper.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
                        playRepeat.setTextColor(Color.WHITE);
                        mRepeating = false;

                    }

                    break;
            }

        }
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
        protected void onConnected(@NonNull final MediaControllerCompat mediaController) {

            mSeekBarAudio.setMediaController(mediaController);
            Log.e(TAG, "MediaBrowserConnection : on Connected + mSeekBarAudio.setMediaController(controller)");
        }

        @Override
        protected void onChildrenLoaded(@NonNull String parentId,
                                        @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);

            Log.e(TAG, "MediaBrowserConnection : onChildrenLoaded");

            final MediaControllerCompat mediaController = getMediaController();
/*

            // Queue up all media items for this simple sample.
            for (final MediaBrowserCompat.MediaItem mediaItem : children) {
                Log.e(TAG, "MediaBrowserConnection : onChildrenLoaded + for(MediaBrowserCompat.MediaItem)"+ mediaItem);
                mediaController.addQueueItem(mediaItem.getDescription());
                Log.e(TAG, "MediaBrowserConnection : onChildrenLoaded + addQueueItem"+ mediaItem.getDescription());
            }
*/

            // Call prepare now so pressing play just works.
            mediaController.getTransportControls().prepare();

            if (mShuffling) {
                mMediaBrowserHelper.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
            }
            if (mRepeating) {
                mMediaBrowserHelper.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
            }
            Log.e(TAG, "MediaBrowserConnection : onChildrenLoaded _ getMediaController.getTransportControls.prepare()");
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
        @SuppressLint("DefaultLocale")
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {

            Log.e(TAG, "onPlaybackStateChanged: monitoring");
            mIsPlaying = playbackState != null &&
                    playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;


            seekStart.setText(String.format("%d : %02d ",
                    TimeUnit.MILLISECONDS.toMinutes(position),
                    TimeUnit.MILLISECONDS.toSeconds(position) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes(position))));


            //mMediaControlsImage.setPressed(mIsPlaying);
/*
            if (playbackState.getState() == PlaybackStateCompat.STATE_PAUSED) {
                if (playPause.getVisibility() == View.INVISIBLE){
                    playPause.setVisibility(View.VISIBLE);
                    playIng.setVisibility(View.INVISIBLE);
                }
            }*/
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat mediaMetadata) {
            if (mediaMetadata == null) {
                return;
            }
            textTitle.setText(
                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            textArtist.setText(
                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            albumArt.setImageBitmap(MusicLibrary.getAlbumBitmap(
                    PlayerActivity.this,
                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)));
            seekStart.setText(String.format(Locale.KOREA,"%d : %02d ",
                    TimeUnit.MILLISECONDS.toMinutes(position),
                    TimeUnit.MILLISECONDS.toSeconds(position) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes(position))));
            seekEnd.setText(String.format(Locale.KOREA,"%d : %02d ",
                    TimeUnit.MICROSECONDS.toMinutes(mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)),
                    TimeUnit.MICROSECONDS.toSeconds(mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MICROSECONDS.
                                    toMinutes(mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)))));

        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            super.onRepeatModeChanged(repeatMode);
        }

        @Override
        public void onShuffleModeChanged(int shuffleMode) {
            super.onShuffleModeChanged(shuffleMode);
        }


        public void progressAnimator(PlaybackStateCompat playbackState){

        }
    }

    public void savePreferences(){
        preferences = getApplicationContext().getSharedPreferences("PlayerPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("shuffle", mShuffling);
        editor.putBoolean("repeat", mRepeating);
        Log.e(TAG, "shared Preferences Saved "+ mShuffling + mRepeating);
        editor.commit();

        // 랜덤재생, 반복재생 설정 저장하기
    }


    public void clearPreferences(){
        preferences = getApplicationContext().getSharedPreferences("PlayerPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();


    }

}
