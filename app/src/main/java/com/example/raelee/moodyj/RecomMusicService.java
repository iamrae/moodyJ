package com.example.raelee.moodyj;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.ArrayList;

public class RecomMusicService extends Service {

    private static final String TAG = "RecomMusicService";
    private final IBinder mBinder = new RecomServiceBinder();
    private MediaPlayer mediaPlayer;
    private boolean isPrepared;
    private int mCurrentPosition;
    ArrayList<RecommendedLists> mList = StreamingMainActivity.arrayRecomList;
    String fileName;
    Intent broadcast;
    RecommendedLists recommendedLists;

    // 오디오 포커스 관리하는 매니저
    AudioManager audioManager;

    public class RecomServiceBinder extends Binder {
        RecomMusicService getService() {

            return RecomMusicService.this;
        }
    }

    public RecomMusicService() {
    }

    @Override
    public void onCreate(){
        super.onCreate();
        broadcast = new Intent();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPrepared = true;
                mp.start();
                broadcast.setAction(BroadcastActions.PREPARED);
                broadcast.putExtra("position", mCurrentPosition);
                broadcast.putExtra("order", 1);
                sendBroadcast(broadcast); // prepared 상태임을 전달
                audioManager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                audioManager.requestAudioFocus(mfocusListener, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN);

            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                isPrepared = false;
                broadcast = new Intent();
                broadcast.setAction(BroadcastActions.PLAY_STATE_CHANGED); // 재생상태 변경됨을 전달
                broadcast.putExtra("position", mCurrentPosition);
                broadcast.putExtra("order", 2);
                sendBroadcast(broadcast);
                return false;
            }
        });
        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {

            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isPrepared = false;
                sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED)); // 재생상태 변경됨을 전달
            }
        });

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        audioManager.abandonAudioFocus(mfocusListener);
    }


    // setSong 같은 거군
    private String getAudioItem(int position) {
        mCurrentPosition = position;
        fileName = mList.get(mCurrentPosition).getFileName();
        // 재생할 목록의 position 값을 받아서 현재 재생할 파일명 가져온다
        Log.e(TAG, "getAudioItem: 오디오아이템 파일명 받기" + fileName);
        return fileName;
    }

    public RecommendedLists getAudioInfo(int position) {
        mCurrentPosition = position;
        recommendedLists = mList.get(mCurrentPosition);
        return recommendedLists;
    }

    private void prepare(String fileName) {
        try {
            String url = "http://ec2-13-125-241-11.ap-northeast-2.compute.amazonaws.com/Music/"; // 수정 서버(190718)
            String ext = ".mp3";
            String dataPath = url + fileName + ext;
            mediaPlayer.setDataSource(dataPath);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stop() {
        mediaPlayer.stop();
        mediaPlayer.reset();
    }


    public void setPlayList(ArrayList<RecommendedLists> arrayList) {
        if (!mList.equals(arrayList)) {
            mList.clear();
            mList.addAll(arrayList);
        }
    }

    public void play(int position) {
        mCurrentPosition = position;
        getAudioItem(mCurrentPosition);
        stop();
        prepare(fileName);
        broadcast = new Intent();
        broadcast.setAction(BroadcastActions.PLAY_STATE_CHANGED); // 재생상태 변경됨을 전달
        broadcast.putExtra("order", 3);
        broadcast.putExtra("position", mCurrentPosition);
        Log.e(TAG, "putExtra Int " + position);
        //sendBroadcast(broadcast);

    }

    public void play() {
        if (isPrepared) {
            mediaPlayer.start();
            sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED)); // 재생상태 변경됨을 전달
        }
    }

    public void pause() {
        if (isPrepared) {
            mediaPlayer.pause();
            sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED)); // 재생상태 변경됨을 전달
        }
    }

    public void forward() {
        if (mList.size() - 1 > mCurrentPosition) {
            mCurrentPosition++; // 다음 포지션으로 이동
        } else {
            mCurrentPosition = 0; // 맨 처음 포지션으로 이동
        }
        play(mCurrentPosition);
    }

    public void prev() {
        if(mCurrentPosition > 0) {
            mCurrentPosition--; // 이전 포지션으로 이동
        } else {
            mCurrentPosition = mList.size() - 1; // 마지막 포지션으로 이동
        }
        play(mCurrentPosition);
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public boolean isLooping() {
        return mediaPlayer.isLooping();
    }

    public class BroadcastActions {
        public final static String PREPARED = "PREPARED";
        public final static String PLAY_STATE_CHANGED = "PLAY_STATE_CHANGED";
    }

    private AudioManager.OnAudioFocusChangeListener mfocusListener =
            new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                            // 재생중인 media 볼륨 처리
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                    7,
                                    AudioManager.FLAG_PLAY_SOUND);
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS:
                            mediaPlayer.pause();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            // 재생중인 미디어 음소거 처리
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                    0, AudioManager.FLAG_PLAY_SOUND);
                            break;

                    }
                }
            };

}
