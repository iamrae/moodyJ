package com.example.raelee.moodyj;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicService extends MediaBrowserServiceCompat {

    private static final String TAG = MusicService.class.getSimpleName();

    private MediaSessionCompat mSession;
    private PlayerAdapter mPlayback;
    private MediaNotificationManager mMediaNotificationManager;
    private MediaSessionCallback mCallback;
    private boolean mServiceInStartedState;
    private int mRepetition, mShuffle;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e(TAG, "onCreate: 시작");

        // Create a new MediaSession.
        mSession = new MediaSessionCompat(this, "MusicService");
        mCallback = new MediaSessionCallback();
        mSession.setCallback(mCallback);
        mSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        Log.e(TAG, "onCreate: 세션토큰 세팅 - " + mSession.getSessionToken());
        setSessionToken(mSession.getSessionToken());

        mMediaNotificationManager = new MediaNotificationManager(this);

        mPlayback = new MediaPlayerAdapter(this, new MediaPlayerListener());
        Log.e(TAG, "onCreate: 서비스가 시작되고 세션과 노티피케이션 매니저를 생성");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e(TAG, "onTaskRemoved  - 서비스 자체종료??");
        stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: 시작, 노티피케이션 매니저 없어지고, 음악재생 멈추고, 세션도 해제한다");
        mMediaNotificationManager.onDestroy();
        mPlayback.stop();
        mSession.release();
        Log.e(TAG, "onDestroy: MediaPlayerAdapter stopped, and MediaSession released");
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName,
                                                           int clientUid,
                                                           Bundle rootHints) {
        Log.e(TAG, "onGetRoot : 브라우저 루트(MusicLibrary.getRoot()) 반환 " + MusicLibrary.getRoot());
        return new MediaBrowserServiceCompat.BrowserRoot(MusicLibrary.getRoot(), null);

    }

    @Override
    public void onLoadChildren(
            @NonNull final String parentMediaId,
            @NonNull final MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
        //Log.e(TAG, "onLoadChildren: result(MusicLibrary.getMediaItems()) 반환" + MusicLibrary.getMediaItems());
        result.sendResult(MusicLibrary.getMediaItems());

    }

/*    @Override
    public void onLoadItem(@NonNull final String mediaId, @NonNull final MediaBrowserServiceCompat.Result<MediaBrowserCompat.MediaItem> result){

        result.sendResult(MusicLibrary.getMediaItems());

    }*/

    // MediaSession Callback: Transport Controls -> MediaPlayerAdapter
    public class MediaSessionCallback extends MediaSessionCompat.Callback {

        private final List<MediaSessionCompat.QueueItem> mPlaylist = new ArrayList<>();
        private int mQueueIndex = -1;
        private MediaMetadataCompat mPreparedMedia; //내거에서는 mMediaMetadataCompat 이라고 되어있나..

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            //Log.e(TAG, "미디어세션콜백 - onAddQueueItem 메소드 실행");
            mPlaylist.add(new MediaSessionCompat.QueueItem(description, description.hashCode())); // description.hashCode()는 뭐지, getMediaId()로 가져올 수는 없나
            mQueueIndex = (mQueueIndex == -1) ? 0 : mQueueIndex;
            // 조건 ? 수식1 : 수식2 ;
            //§조건을 평가해서 true이면 수식1을 false이면 수식2를 실행한다.
            // mQueueIndex 값이 -1이라면 mQueueIndex = 0 으로 변경, -1이 아니라면 원래 값으로 유지
            //Log.e(TAG, "onAddQueueItem: " + mQueueIndex + " 그리고 mPlaylist를 세션 Queue에 담는다");
            mSession.setQueue(mPlaylist);
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            //Log.e(TAG, "미디어 세션 콜백 - onRemoveQueueItem 메소드 실행");
            mPlaylist.remove(new MediaSessionCompat.QueueItem(description, description.hashCode()));
            Log.e(TAG, "미디어세션콜백 - onRemoveQueueItem / 플레이리스트에서 queueItem 삭제" + mPlaylist);
            mQueueIndex = (mPlaylist.isEmpty()) ? -1 : mQueueIndex;
            Log.e(TAG, "onAddQueueItem: " + mQueueIndex + " 그리고 mPlaylist를 세션 Queue에 담는다");
            mSession.setQueue(mPlaylist);
        }


        @Override
        public void onPrepare() {

            if (mQueueIndex < 0 && mPlaylist.isEmpty()) {
                // Nothing to play.
                Log.e(TAG, "mQueueIndex가 0보다 작고 mPlaylist가 비어있을 때");
                return;
            }


            final String mediaId = mPlaylist.get(mQueueIndex).getDescription().getMediaId();
            Log.e(TAG, "onPrepare: 포지션 값 : " + mQueueIndex );
            //final String mediaId = String.valueOf(mQueueIndex);
            // Log.e(TAG, "미디어 세션 콜백 - onPrepare 메소드 / queueIndex 값에 해당하는 MediaId를 불러와서 String mediaId에 지정" + mediaId);
            mPreparedMedia = MusicLibrary.getMetadata(MusicService.this, mediaId);
            Log.e(TAG, "미디어 세션 콜백 - onPrepare 메소드 / mediaId를 기준으로 MusicLibrary 에 있는 metadata 불러와서 PrepareMedia 에 저장");
            mSession.setMetadata(mPreparedMedia);
            Log.e(TAG, "onPrepare()" + mPreparedMedia);

            if (!mSession.isActive()) {
                Log.e(TAG, "미디어세션이 활성화되어있지 않을 경우 세션 활성화시킴");
                mSession.setActive(true);
            }
        }

        @Override
        public void onPlay() {
            if (!isReadyToPlay()) {
                Log.e(TAG, "재생준비가 되어 있지 않을때 리턴");
                // Nothing to play.
                return;
            }

            if (mPreparedMedia == null) {
                Log.e(TAG, "mPrepareMedia(metadataCompat)이 비어있을 때는 onPrepare 메소드 실행");
                onPrepare();
            }

            mPlayback.playFromMedia(mPreparedMedia);
            Log.e(TAG, "playFromMedia 메소드에 metadata 값을 넣어서 노래를 재생한다");
        }


        @Override
        public void onPause() {
            Log.e(TAG, "노래 일시정지");
            mPlayback.pause();
        }

        @Override
        public void onStop() {
            Log.e(TAG, "노래 멈추기 + 세션 비활성화하기");
            mPlayback.stop();
            mSession.setActive(false);
        }

        @Override
        public void onSkipToNext() {
            Log.e(TAG, "onSkipToNext 다음곡 재생 " + mQueueIndex);

            if(mShuffle == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                Log.e(TAG, "랜덤재생 모드");
                // 셔플 모드가 실행되어있는 경우 랜덤한 곡을 이어서 재생한다.
                mCallback.shuffleAll();
            } else {
            mQueueIndex = (++mQueueIndex % mPlaylist.size()); // 인덱스 값 하나 더해주고 그 걸 재생목록 사이즈로 나눈 나머지 값을 다시 index에 저장
            Log.e(TAG, "인덱스 값 + 1 하고 재생목록 사이즈로 나눈 나머지 값은 " + mQueueIndex);
            mPreparedMedia = null;
            Log.e(TAG, "mPreparedMedia 를 비우고 onPlay 메소드 실행 ");
            onPlay();
            }
        }

        @Override
        public void onSkipToPrevious() {

            if(mShuffle == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                Log.e(TAG, "랜덤재생 모드");
                // 셔플 모드가 실행되어있는 경우 랜덤한 곡을 이어서 재생한다.
                mCallback.shuffleAll();
            } else {
                Log.e(TAG, "onSkipToPrevious 이전곡 재생 " + mQueueIndex);
                mQueueIndex = mQueueIndex > 0 ? mQueueIndex - 1 : mPlaylist.size() - 1;
                Log.e(TAG, "인덱스 값이 0보다 크면 인덱스를 -1로 지정하고, 그렇지 않으면 재생목록 사이즈에서 1을 뺀다 " + mQueueIndex);
                mPreparedMedia = null;
                Log.e(TAG, "mPreparedMedia 를 비우고 onPlay 메소드 실행 ");
                onPlay();
            }
        }

        @Override
        public void onSkipToQueueItem(long id) {
            Log.e(TAG, "onSkipToQueueItem 재생 " + mQueueIndex);
            mQueueIndex = (int)id;
            mPreparedMedia = null;
            onPlay();
        }

        @Override
        public void onSeekTo(long pos) {
            Log.e(TAG, "onSeekTo / seek bar 이동시 해당 위치에서 곡 재생하기");
            mPlayback.seekTo(pos);
            mPlayback.go();
            //mPlayback.play();
        }

        private boolean isReadyToPlay() {
            Log.e(TAG, "재생준비 여부 반환");
            return (!mPlaylist.isEmpty());
        }

        @Override
        public void onSetRepeatMode(int repeatMode) {
            super.onSetRepeatMode(repeatMode);
            Log.e(TAG, "onSetRepeatMode 한곡 반복 모드 실행하기");

            // 액티비티에서 반복재생 버튼을 누르면 repeatMode 표시하는 정수를 본 메소드에 전달
            // mRepetition이라는 변수에 받은 값을 전달하고, onCompletionListener 에서 해당 값을 확인할 수 있게 한다.
            mRepetition = repeatMode;

        }

        public void repeatOnce(){
            // 실제로 한 곡만 반복하여 재생하게 하는 메소드

            Log.e(TAG, "repeatOnce 메소드 실행");
            mQueueIndex = (mQueueIndex % mPlaylist.size()); // 현재 재생되고 있는 곡의 인덱스값을 변수에 다시 지정
            Log.e(TAG,  ""+mQueueIndex);
            mPreparedMedia = null;
            Log.e(TAG, "mPreparedMedia 를 비우고 onPlay 메소드 실행 ");
            onPlay();

        }

        @Override
        public void onSetShuffleMode(int shuffleMode) {
            super.onSetShuffleMode(shuffleMode);
            Log.e(TAG, "onSetShuffleMode 랜덤재생 모드 실행하기");

            // 액티비티에서 넘겨받은 shuffleMode 정수를 mShuffle이라는 변수에 지정
            mShuffle = shuffleMode;
        }

        public void shuffleAll(){
            // 실제로 한 곡만 반복하여 재생하게 하는 메소드
            Log.e(TAG, "shuffleAll 메소드 실행");

            Random random = new Random();
            mQueueIndex = random.nextInt((mPlaylist.size() - 1) - 0 + 1) + 0; // 랜덤한 정수를 뽑아내어 인덱스값에 지정
            Log.e(TAG,  ""+mQueueIndex);
            mPreparedMedia = null;
            Log.e(TAG, "mPreparedMedia 를 비우고 onPlay 메소드 실행 ");
            onPlay();
        }

    }

    // MediaPlayerAdapter Callback: MediaPlayerAdapter state -> MusicService.
    // MediaPlayerAdapter 에서 재생상태 여부를 서비스로 가져온다
    public class MediaPlayerListener extends PlaybackInfoListener {

        private final ServiceManager mServiceManager;

        MediaPlayerListener() {
            Log.e(TAG, " MediaPlayerListener = 서비스 매니저 생성 및 초기화");
            mServiceManager = new ServiceManager();
        }

        @Override
        public void onPlaybackStateChange(PlaybackStateCompat state) {
            // Report the state to the MediaSession.
            mSession.setPlaybackState(state);
            Log.e(TAG, "세션에서 재생상태 세팅하기");

            // Manage the started state of this service.
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    Log.e(TAG, "playbackStateCompat 에서 재생중이라고 표시할 경우, moveServiceToStartedState 메소드 실행");
                    mServiceManager.moveServiceToStartedState(state);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    Log.e(TAG, "playbackStateCompat 에서 일시정지됨이라고 표시할 경우, updateNotificationForPause 메소드 실행");
                    mServiceManager.updateNotificationForPause(state);
                    break;
                case PlaybackStateCompat.STATE_STOPPED:
                    Log.e(TAG, "playbackStateCompat 에서 정지됨이라고 표시할 경우,  moveServiceOutOfStartedState 메소드 실행");
                    mServiceManager.moveServiceOutOfStartedState(state);
                    break;
            }
        }

        @Override
        public void onPlaybackCompleted() {
            // 한 곡 재생 완료되었을 때 실행할 메소드

            Log.e(TAG, "onPlaybackCompleted 시작");

            if (mRepetition == PlaybackStateCompat.REPEAT_MODE_ONE) {
                Log.e(TAG, "한곡반복 모드");
                // 반복재생 모드가 실행되어있는 경우 한곡 반복 메소드를 실행한다.
                mCallback.repeatOnce();
            } else if(mShuffle == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                Log.e(TAG, "랜덤재생 모드");
                // 셔플 모드가 실행되어있는 경우 랜덤한 곡을 이어서 재생한다.
                mCallback.shuffleAll();
            } else {
                // 한 곡 재생이 끝나면 바로 다음 곡을 이어 재생한다.
                mCallback.onSkipToNext();
            }
        }

        class ServiceManager {

            private void moveServiceToStartedState(PlaybackStateCompat state) {
                Log.e(TAG, "moveServiceOutOfStartedState 메소드 시작");
                Notification notification =
                        mMediaNotificationManager.getNotification(
                                mPlayback.getCurrentMedia(), state, getSessionToken());
                // getNotification 메소드는 (metadata, 재생상태, 토큰) 값을 필요로 한다.
                Log.e(TAG, "notificationManager가 metadata, 재생상태, 토큰 값을 주고 노티피케이션을 받아온다");

                if (!mServiceInStartedState) {
                    Log.e(TAG, "mServiceInStartedState 가 아닐 떄 foregroundService 를 시작한다");
                    ContextCompat.startForegroundService(
                            MusicService.this,
                            new Intent(MusicService.this, MusicService.class));
                    // foregroundService 메소드 실행할 때 context와 intent 를 필요로 한다.
                    mServiceInStartedState = true;
                }

                startForeground(MediaNotificationManager.NOTIFICATION_ID, notification);
                Log.e(TAG, "노티피케이션 창 누르면 Foreground 로 화면 불러온다 + 노티 아이디값은 " + MediaNotificationManager.NOTIFICATION_ID);
            }

            private void updateNotificationForPause(PlaybackStateCompat state) {
                Log.e(TAG, "updateNotificationForPause 메소드 시작");
                stopForeground(false);
                Log.e(TAG, "stopForeground + Notification 을 없애진 않는다");
                Notification notification =
                        mMediaNotificationManager.getNotification(
                                mPlayback.getCurrentMedia(), state, getSessionToken());
                mMediaNotificationManager.getNotificationManager()
                        .notify(MediaNotificationManager.NOTIFICATION_ID, notification);
                Log.e(TAG, "노티피케이션매니저에게 노티값 전달? 노티 아이디값은 " + MediaNotificationManager.NOTIFICATION_ID);
            }

            private void moveServiceOutOfStartedState(PlaybackStateCompat state) {
                Log.e(TAG, "moveServiceOutofStartedSTate 호출되면 foreground 정지하고 서비스 종료하는듯");
                stopForeground(true);
                stopSelf();
                mServiceInStartedState = false;
            }
        }

    }

}