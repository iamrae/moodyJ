package com.example.raelee.moodyj;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

/**
 * Abstract player implementation that handles playing music with proper handling of headphones
 * and audio focus.
 */
// 음악 재생과 오디오포커스 등을 관리하는 Abstract Player Implementation

public abstract class PlayerAdapter {
    private static final String TAG = PlayerAdapter.class.getSimpleName();

    private static final float MEDIA_VOLUME_DEFAULT = 1.0f;
    private static final float MEDIA_VOLUME_DUCK = 0.2f;

    private static final IntentFilter AUDIO_NOISY_INTENT_FILTER =
            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    private boolean mAudioNoisyReceiverRegistered = false;
    private final BroadcastReceiver mAudioNoisyReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                        if (isPlaying()) {
                            pause();
                        }
                    }
                }
            };

    private final Context mApplicationContext;
    private final AudioManager mAudioManager;
    private final AudioFocusHelper mAudioFocusHelper;

    private boolean mPlayOnAudioFocus = false;

    public PlayerAdapter(@NonNull Context context) {
        Log.e(TAG, "PlayerAdapter 시작");
        mApplicationContext = context.getApplicationContext();
        mAudioManager = (AudioManager) mApplicationContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioFocusHelper = new AudioFocusHelper();
        Log.e(TAG, "ApplicationContext 지정, 시스템서비스로부터 오디오서비스 컨텍스트 받아오고, audioFocusHelper 생성");
    }

    // metadata (mediaId 값) 받아서 음악 재생하는 메소드
    public abstract void playFromMedia(MediaMetadataCompat metadata);

    // 현재 재생중인 음악 반환하는 메소드
    public abstract MediaMetadataCompat getCurrentMedia();

    // 재생 여부 확인하는 메소드
    public abstract boolean isPlaying();

    // 재생 시작하는 메소드 : AudioFocus 등의 정보를 받는 Receiver 등록
    public final void play() {
        if (mAudioFocusHelper.requestAudioFocus()) {
            registerAudioNoisyReceiver();
            onPlay();
        }
    }



    /**
     * Called when media is ready to be played and indicates the app has audio focus.
     * 음악 재생 준비가 되고, 오디오 포커스를 확인한 뒤에 실제로 음악을 재생한다
     */
    protected abstract void onPlay();


    // 음악 일시정지 메소드 : AudioFocus 정보 버리기 + Receiver 해제하기 + 음악 일시정지하기
    public final void pause() {
        if (!mPlayOnAudioFocus) {
            mAudioFocusHelper.abandonAudioFocus();
        }

        unregisterAudioNoisyReceiver();
        onPause();
    }

    /**
     * Called when media must be paused.
     * 음악재생을 실제로 일시정지 할 때 호출된다
     */
    protected abstract void onPause();

    // 음악 정지 메소드 : AudioFocus 정보 버리기 + Receiver 해제하기 + 음악 정지하기
    public final void stop() {
        mAudioFocusHelper.abandonAudioFocus();
        unregisterAudioNoisyReceiver();
        onStop();
    }

    /**
     * Called when the media must be stopped. The player should clean up resources at this
     * point.
     * 음악을 실제로 멈출 때 호출된다
     */
    protected abstract void onStop();

    public abstract void seekTo(long position);

    public abstract void go();

    public abstract void setVolume(float volume);

    // AudioNoisyReceiver 등록 : 인텐트 필터로 음악 재생에 필요한 앱 외부 정보를 받아오는 Receiver 를 등록
    private void registerAudioNoisyReceiver() {
        if (!mAudioNoisyReceiverRegistered) {
            mApplicationContext.registerReceiver(mAudioNoisyReceiver, AUDIO_NOISY_INTENT_FILTER);
            mAudioNoisyReceiverRegistered = true;
        }
    }

    // AudioNoisyReceiver 등록 : AudioNoisyReceiver 를 등록 해제
    private void unregisterAudioNoisyReceiver() {
        if (mAudioNoisyReceiverRegistered) {
            mApplicationContext.unregisterReceiver(mAudioNoisyReceiver);
            mAudioNoisyReceiverRegistered = false;
        }
    }

    /**
     * Helper class for managing audio focus related tasks.
     * 오디오 포커스 관련된 tasks
     */
    private final class AudioFocusHelper
            implements AudioManager.OnAudioFocusChangeListener {

        // AudioFocus 요청
        private boolean requestAudioFocus() {
            final int result = mAudioManager.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }

        private void abandonAudioFocus() {
            mAudioManager.abandonAudioFocus(this);
        }

        // AudioFocus 상태 변경시 일어나는 일들
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mPlayOnAudioFocus && !isPlaying()) {
                        play();
                    } else if (isPlaying()) {
                        setVolume(MEDIA_VOLUME_DEFAULT);
                    }
                    mPlayOnAudioFocus = false;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    setVolume(MEDIA_VOLUME_DUCK);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (isPlaying()) {
                        mPlayOnAudioFocus = true;
                        pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    mAudioManager.abandonAudioFocus(this);
                    mPlayOnAudioFocus = false;
                    stop();
                    break;
            }
        }
    }
}
