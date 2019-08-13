package com.example.raelee.moodyj;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

/**
 * Exposes the functionality of the {@link MediaPlayer} and implements the {@link PlayerAdapter}
 * so that {@link PlayerActivity} can control music playback.
 * MediaPlayer 의 주요 기능을 담은 클래스, PlayerAdapter를 참조(implement)하여 재생화면에서 Playback 을 컨트롤할 수 있게 한다.
 */
public final class MediaPlayerAdapter extends PlayerAdapter {

    private static final String TAG = MediaPlayerAdapter.class.getSimpleName();

    private final Context mContext;
    private MediaPlayer mMediaPlayer;
    private String mMediaId;
    private PlaybackInfoListener mPlaybackInfoListener;
    private MediaMetadataCompat mCurrentMedia;
    private int mState;
    private boolean mCurrentMediaPlayedToCompletion;

    // Work-around for a MediaPlayer bug related to the behavior of MediaPlayer.seekTo()
    // while not playing.
    private int mSeekWhileNotPlaying = -1;

    public MediaPlayerAdapter(Context context, PlaybackInfoListener listener) {
        super(context);
        mContext = context.getApplicationContext();
        mPlaybackInfoListener = listener;
    }

    /**
     * Once the {@link MediaPlayer} is released, it can't be used again, and another one has to be
     * created. In the onStop() method of the {@link PlayerActivity} the {@link MediaPlayer} is
     * released. Then in the onStart() of the {@link PlayerActivity} a new {@link MediaPlayer}
     * object has to be created. That's why this method is private, and called by load(int) and
     * not the constructor.
     *
     * MediaPlayer 가 한번 Release 되면 해당 플레이어는 재사용할 수 없다. onStop 에서 release 시킨다면 해당 액티비티의 onStart 에서 플레이어를 새로 생성해줘야 한다.
     * 그래서 initializing Method 가 Private method 임
     */
    private void initializeMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mPlaybackInfoListener.onPlaybackCompleted();
                    Log.e(TAG, "initialized MediaPlayer on completion");

                    // Set the state to "paused" because it most closely matches the state
                    // in MediaPlayer with regards to available state transitions compared
                    // to "stop".
                    // Paused allows: seekTo(), start(), pause(), stop()
                    // Stop allows: stop()
                    // 미디어플레이어가 초기화될 때 첫 상태는 pause 상태로 지정해주어야 한다.
                    // stop 상태로 초기화하면 다른 seekTo, start 등의 다른 메소드를 사용할 수 없음

                   //setNewState(PlaybackStateCompat.STATE_PAUSED);
                   //mMediaPlayer.reset();

                }
            });
        }
    }


    // Implements PlaybackControl.
    @Override
    public void playFromMedia(MediaMetadataCompat metadata) {
        // 현재 재생 중인 곡의 mediaId 를 받아서 뮤직 라이브러리에 해당 ID 를 가진 곡의 정보를 요청, 노래를 재생한다.
        mCurrentMedia = metadata;
        Log.e(TAG, "playFromMedia(metadata) " + mCurrentMedia);
        final String mediaId = metadata.getDescription().getMediaId();
        playFile(mediaId);
        Log.e(TAG, "playFromMedia(metadata) / playFile(MusicLibrary.getMusicFilename(mediaId) " + mediaId);
    }

    @Override
    public MediaMetadataCompat getCurrentMedia() {
        // 현재 재생 중인 곡의 metadata
        return mCurrentMedia;
    }

/*
    private void playFile(String filename) {
        boolean mediaChanged = (mFilename == null || !filename.equals(mFilename));
        if (mCurrentMediaPlayedToCompletion) {
            // Last audio file was played to completion, the resourceId hasn't changed, but the
            // player was released, so force a reload of the media file for playback.
            // 한 곡 재생이 끝났을 경우에 리소스는 남아있지만 플레이어는 released 되어버려서 새 미디어파일을 얹어주어야 한다.
            mediaChanged = true;
            mCurrentMediaPlayedToCompletion = false;
        }
        if (!mediaChanged) {
            if (!isPlaying()) {
                play();
            }
            return;
        } else {
            release();
        }

        mFilename = filename;
        Log.e(TAG, "playFile() " + mFilename);

        initializeMediaPlayer();
        Log.e(TAG, "playFile() initializing MediaPlayer");

        try {
            // 이 부분이 실제로 음악 파일을 불러와서 재생하는 부분이.

            AssetFileDescriptor assetFileDescriptor = mContext.getAssets().openFd(mFilename);
            mMediaPlayer.setDataSource(
                    assetFileDescriptor.getFileDescriptor(),
                    assetFileDescriptor.getStartOffset(),
                    assetFileDescriptor.getLength());
            Log.e(TAG, "setDataSource " + assetFileDescriptor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to open file: " + mFilename, e);
        }

        try {
            mMediaPlayer.prepare();
        } catch (Exception e) {
            throw new RuntimeException("Failed to open file: " + mFilename, e);
        }

        play();
    }
*/


    // mediaId 값을 받아와서 그 아이디를 가진 파일을 외부저장소에서 찾아서 노래를 재생해준다.
    private void playFile(String mediaId) {
        boolean mediaChanged = (mMediaId == null || !mediaId.equals(mMediaId));
        Log.e(TAG, "playFile() mediaChanged??" + mediaChanged);
        if (mCurrentMediaPlayedToCompletion) {
            // Last audio file was played to completion, the resourceId hasn't changed, but the
            // player was released, so force a reload of the media file for playback.
            // 한 곡 재생이 끝났을 경우에 리소스는 남아있지만 플레이어는 released 되어버려서 새 미디어파일을 얹어주어야 한다.
            mediaChanged = true;
            Log.e(TAG, "playFile() mediaChanged + and playtoCompleted??" + mediaChanged);
            mCurrentMediaPlayedToCompletion = false;
        }
        if (!mediaChanged) {
            Log.e(TAG, "playFile 에서 mediaChanged 안된 경우");
            if (!isPlaying()) {
                Log.e(TAG, "playFile 에서 mediaChanged 안된 경우 + 현재 음악이 재생중이지 않은 경우");
                play();
            }
            return;
        } else {
            release();
        }

        mMediaId = mediaId;
        long getUri = Long.parseLong(mMediaId);
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, getUri);
        Log.e(TAG, "playFile() " + mMediaId + " " + getUri + " " + trackUri);

        initializeMediaPlayer();
        Log.e(TAG, "playFile() initializing MediaPlayer");

        try {
            // 실제로 음악 파일을 불러와서 재생하는 부분
            // 파일을 불러오는 저장소 변경
            // getAsset()... 하면 내 저장소에서 가져오는거고, getContentResolver().openFile... 하면 외부저장소에서 가져옴
            AssetFileDescriptor assetFileDescriptor = mContext.getContentResolver().openAssetFileDescriptor(trackUri, "r");
            mMediaPlayer.setDataSource(
                    assetFileDescriptor.getFileDescriptor(),
                    assetFileDescriptor.getStartOffset(),
                    assetFileDescriptor.getLength());
            Log.e(TAG, "setDataSource " + assetFileDescriptor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to open file: " + mMediaId, e);
        }

        try {
            mMediaPlayer.prepare();
        } catch (Exception e) {
            throw new RuntimeException("Failed to open file: " + mMediaId, e);
        }

        play();
    }

    @Override
    public void onStop() {
        // Regardless of whether or not the MediaPlayer has been created / started, the state must
        // be updated, so that MediaNotificationManager can take down the notification.
        setNewState(PlaybackStateCompat.STATE_STOPPED);
        release();
    }

    private void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    @Override
    protected void onPlay() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            setNewState(PlaybackStateCompat.STATE_PLAYING);
            Log.e(TAG, "onPlay() setting New State " + PlaybackStateCompat.STATE_PLAYING);
        }
    }

    @Override
    protected void onPause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            setNewState(PlaybackStateCompat.STATE_PAUSED);
        }
    }

    @Override
    public void go() {
        mMediaPlayer.start();
        setNewState(PlaybackStateCompat.STATE_PLAYING);
    }

    // This is the main reducer for the player state machine.
    private void setNewState(@PlaybackStateCompat.State int newPlayerState) {
        mState = newPlayerState;

        Log.e(TAG, "setNewState 메소드 실행 " + mState );
        // Whether playback goes to completion, or whether it is stopped, the
        // mCurrentMediaPlayedToCompletion is set to true.
        if (mState == PlaybackStateCompat.STATE_STOPPED) {
            mCurrentMediaPlayedToCompletion = true;
        }

        Log.e(TAG, "setNewState에서 mSeekWhileNotPlaying은 " + mSeekWhileNotPlaying);
        // Work around for MediaPlayer.getCurrentPosition() when it changes while not playing.
        final long reportPosition;
        if (mSeekWhileNotPlaying >= 0) {
            reportPosition = mSeekWhileNotPlaying;
            Log.e(TAG, "reporting position "+ mSeekWhileNotPlaying);
            Log.e(TAG, "reporting position" + reportPosition);

            if (mState == PlaybackStateCompat.STATE_PLAYING){
                mSeekWhileNotPlaying = -1;
            }
        } else {
            reportPosition = mMediaPlayer == null ? 0 : mMediaPlayer.getCurrentPosition();
        }

        Log.e(TAG, "setNewState에서 mSeekWhileNotPlaying은 " + mSeekWhileNotPlaying + "reportPosition은 " + reportPosition);

        final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setActions(getAvailableActions());
        stateBuilder.setState(mState,
                reportPosition,
                1.0f,
                SystemClock.elapsedRealtime());
        mPlaybackInfoListener.onPlaybackStateChange(stateBuilder.build());
        Log.e(TAG, "stateBuilder에서 mState는 " + mState + "reportPosition은 " + reportPosition);
    }

    /**
     * Set the current capabilities available on this session. Note: If a capability is not
     * listed in the bitmask of capabilities then the MediaSession will not handle it. For
     * example, if you don't want ACTION_STOP to be handled by the MediaSession, then don't
     * included it in the bitmask that's returned.
     */
    @PlaybackStateCompat.Actions
    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        switch (mState) {
            case PlaybackStateCompat.STATE_STOPPED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE;
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                actions |= PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_SEEK_TO
                        | PlaybackStateCompat.SHUFFLE_MODE_ALL
                        | PlaybackStateCompat.REPEAT_MODE_ONE;
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_SEEK_TO
                        | PlaybackStateCompat.SHUFFLE_MODE_ALL
                        | PlaybackStateCompat.REPEAT_MODE_ONE;
                break;
            default:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }

    @Override
    public void seekTo(long position) {
        if (mMediaPlayer != null) {
            if (!mMediaPlayer.isPlaying()) {
                mSeekWhileNotPlaying = (int) position;
                Log.e(TAG, "seekTo + mMediaPlayer is not playing. so mSeekWhileNotPlaying equals " + mSeekWhileNotPlaying);
            }
            mMediaPlayer.seekTo((int) position);
            Log.e(TAG, "mMediaPlayer" + (int)position);

            // Set the state (to the current state) because the position changed and should
            // be reported to clients.
            setNewState(mState);
        }
    }

    @Override
    public void setVolume(float volume) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(volume, volume);
        }
    }
}
