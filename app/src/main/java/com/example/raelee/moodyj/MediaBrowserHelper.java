package com.example.raelee.moodyj;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MediaBrowserHelper {

    // 미디어 브라우저 연결 관련 클래스
    // 브라우저 연결, 콜백, Subscription, Root/Children 등의 일을 다 처리해줌

        private static final String TAG = MediaBrowserHelper.class.getSimpleName();

        private final Context mContext;
        private final Class<? extends MediaBrowserServiceCompat> mMediaBrowserServiceClass;

        private final List<MediaControllerCompat.Callback> mCallbackList = new ArrayList<>();

        private final MediaBrowserConnectionCallback mMediaBrowserConnectionCallback;
        private final MediaControllerCallback mMediaControllerCallback;
        private final MediaBrowserSubscriptionCallback mMediaBrowserSubscriptionCallback;

        private MediaBrowserCompat mMediaBrowser;

        @Nullable
        private MediaControllerCompat mMediaController;

        public MediaBrowserHelper(Context context,
                                  Class<? extends MediaBrowserServiceCompat> serviceClass) {
            Log.e(TAG, "MediaBrowserHelper 생성자 들어옴");
            mContext = context;
            mMediaBrowserServiceClass = serviceClass;

            mMediaBrowserConnectionCallback = new MediaBrowserConnectionCallback();
            mMediaControllerCallback = new MediaControllerCallback();
            mMediaBrowserSubscriptionCallback = new MediaBrowserSubscriptionCallback();
        }

        public void onStart() {
            // 액티비티의 onStart()에서 해당 메소드 실행
            Log.e(TAG, "onStart 시작");
            if (mMediaBrowser == null) {
                mMediaBrowser =
                        new MediaBrowserCompat(
                                mContext,
                                new ComponentName(mContext, mMediaBrowserServiceClass),
                                mMediaBrowserConnectionCallback,
                                null);
                mMediaBrowser.connect();
                Log.e(TAG, "onStart(): connecting mMediaBrowser");
            }
            Log.e(TAG, "onStart: Creating MediaBrowser, and connecting");
        }

        public void onStop() {
            // 액티비티의 onStop()에서 해당 메소드 실행
            Log.e(TAG, "onStop 시작");
            if (mMediaController != null) {
                Log.e(TAG, "미디어컨트롤러 null 아님");
                mMediaController.unregisterCallback(mMediaControllerCallback);
                mMediaController = null;
            }
            if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
                Log.e(TAG, "브라우저 null 아님 + 브라우저 연결됨");
                mMediaBrowser.disconnect();
                mMediaBrowser = null;
            }
            Log.e(TAG, "reset State 시작");
            resetState();

            Log.e(TAG, "onStop: Releasing MediaController, Disconnecting from MediaBrowser");
        }

        /**
         * Called after connecting with a {@link MediaBrowserServiceCompat}.
         * <p>
         * Override to perform processing after a connection is established.
         *
         * @param mediaController {@link MediaControllerCompat} associated with the connected
         *                        MediaSession.
         */
        protected void onConnected(@NonNull MediaControllerCompat mediaController) {

            Log.e(TAG, "HelperClass의 onConnected : 내용은 없고 메인에서 가져다 씀");

        }

        /**
         * Called after loading a browsable {@link MediaBrowserCompat.MediaItem}
         *
         * @param parentId The media ID of the parent item.
         * @param children List (possibly empty) of child items.
         */
        protected void onChildrenLoaded(@NonNull String parentId,
                                        @NonNull List<MediaBrowserCompat.MediaItem> children) {

            Log.e(TAG, "HelperClass의 onChildrenLoaded : 내용은 없고 액티비티에서 호출");
        }

        /**
         * Called when the {@link MediaBrowserServiceCompat} connection is lost.
         */
        protected void onDisconnected() {
            Log.e(TAG, "HelperClass의 onDisconnected : 내용은 없고 액티비티에서 호출");
        }

        @NonNull
        protected final MediaControllerCompat getMediaController() {
            Log.e(TAG, "MediaControllerCompat");
            if (mMediaController == null) {
                Log.e(TAG, "미디어 컨트롤러가 비어있음(null)");
                throw new IllegalStateException("MediaController is null!");
            }
            Log.e(TAG, "미디어 컨트롤러 내보내기");
            return mMediaController;
        }

        /**
         * The internal state of the app needs to revert to what it looks like when it started before
         * any connections to the {@link MusicService} happens via the {@link MediaSessionCompat}.
         */
        private void resetState() {
            Log.e(TAG, "reset State (재생상태??)");
            performOnAllCallbacks(new CallbackCommand() {
                @Override
                public void perform(@NonNull MediaControllerCompat.Callback callback) {
                    Log.e(TAG, "재생상태");
                    callback.onPlaybackStateChanged(null);
                }
            });
        }


        public MediaControllerCompat.TransportControls getTransportControls() {

            Log.e(TAG, "getTrasnportControl : 미디어플레이어를 직접 조절");
            if (mMediaController == null) {
                Log.d(TAG, "getTransportControls: MediaController is null!");
                throw new IllegalStateException("MediaController is null!");
            }

            Log.e(TAG, "getTrasnportControl 내보내기");
            return mMediaController.getTransportControls();
        }

        public void registerCallback(MediaControllerCompat.Callback callback) {
            if (callback != null) {
                Log.e(TAG, "콜백리스트 추가?? 뭐지 " + callback);
                mCallbackList.add(callback);

                // Update with the latest metadata/playback state.
                if (mMediaController != null) {
                    Log.e(TAG, "mediaController null이 아님");
                    final MediaMetadataCompat metadata = mMediaController.getMetadata();
                    if (metadata != null) {
                        Log.e(TAG, "metadata null 아님");
                        callback.onMetadataChanged(metadata);
                    }

                    final PlaybackStateCompat playbackState = mMediaController.getPlaybackState();
                    if (playbackState != null) {
                        Log.e(TAG, "재생상태도 있음");
                        callback.onPlaybackStateChanged(playbackState);
                    }

                }
            }
        }

        private void performOnAllCallbacks(@NonNull CallbackCommand command) {
            for (MediaControllerCompat.Callback callback : mCallbackList) {
                Log.e(TAG, "콜백있을때마다 실행하기?");
                if (callback != null) {
                    command.perform(callback);
                    Log.e(TAG, "콜백이 null이 아닐 때 performOnAllCallbacks: " + callback);
                }
            }
        }

        /**
         * Helper for more easily performing operations on all listening clients.
         */
        private interface CallbackCommand {
            void perform(@NonNull MediaControllerCompat.Callback callback);
        }

        // Receives callbacks from the MediaBrowser when it has successfully connected to the
        // MediaBrowserService (MusicService).
        private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {

            // Happens as a result of onStart().
            @Override
            public void onConnected() {
                Log.e(TAG, "브라우저커넥션 콜백 onConnected");
                try {
                    // Get a MediaController for the MediaSession.
                    mMediaController =
                            new MediaControllerCompat(mContext, mMediaBrowser.getSessionToken());
                    Log.e(TAG, "미디어브라우저가 세션토큰 가져옴");
                    mMediaController.registerCallback(mMediaControllerCallback);
                    Log.e(TAG, "콜백 등록??");

                    // Sync existing MediaSession state to the UI.
                    mMediaControllerCallback.onMetadataChanged(mMediaController.getMetadata());
                    Log.e(TAG, "onConnected: 메타데이터 동기화 sync mediaSession state to the UI + Metadata " + mMediaController.getMetadata());
                    mMediaControllerCallback.onPlaybackStateChanged(
                            mMediaController.getPlaybackState());
                    Log.e(TAG, "onConnected: 재생상태 확인하기 ");

                    MediaBrowserHelper.this.onConnected(mMediaController);
                } catch (RemoteException e) {
                    Log.d(TAG, String.format("onConnected: Problem: %s", e.toString()));
                    throw new RuntimeException(e);
                }

                mMediaBrowser.subscribe(mMediaBrowser.getRoot(), mMediaBrowserSubscriptionCallback);
                Log.e(TAG, "onConnected: subscribing mMediaBrowser.getRoot()");
            }
        }

        // Receives callbacks from the MediaBrowser when the MediaBrowserService has loaded new media
        // that is ready for playback.
        public class MediaBrowserSubscriptionCallback extends MediaBrowserCompat.SubscriptionCallback {

            @Override
            public void onChildrenLoaded(@NonNull String parentId,
                                         @NonNull List<MediaBrowserCompat.MediaItem> children) {
                MediaBrowserHelper.this.onChildrenLoaded(parentId, children);
                Log.e(TAG, "헬퍼클래스 : subscriptionCallback : childrenLoaded");
            }
        }

        // Receives callbacks from the MediaController and updates the UI state,
        // i.e.: Which is the current item, whether it's playing or paused, etc.
        private class MediaControllerCallback extends MediaControllerCompat.Callback {

            @Override
            public void onRepeatModeChanged(final int repeatMode) {
                super.onRepeatModeChanged(repeatMode);
                performOnAllCallbacks(new CallbackCommand() {
                    @Override
                    public void perform(@NonNull MediaControllerCompat.Callback callback) {
                        callback.onRepeatModeChanged(repeatMode);
                    }
                });
            }

            @Override
            public void onShuffleModeChanged(final int shuffleMode) {
                super.onRepeatModeChanged(shuffleMode);
                performOnAllCallbacks(new CallbackCommand() {
                    @Override
                    public void perform(@NonNull MediaControllerCompat.Callback callback) {
                        callback.onRepeatModeChanged(shuffleMode);
                    }
                });
            }

            @Override
            public void onMetadataChanged(final MediaMetadataCompat metadata) {
                Log.e(TAG, "헬퍼클래스 : 메타데이터 변경됨");
                performOnAllCallbacks(new CallbackCommand() {
                    @Override
                    public void perform(@NonNull MediaControllerCompat.Callback callback) {
                        callback.onMetadataChanged(metadata);
                    }
                });
            }

            @Override
            public void onPlaybackStateChanged(@Nullable final PlaybackStateCompat state) {
                Log.e(TAG, "헬퍼클래스 : 재생상태 변경됨");
                performOnAllCallbacks(new CallbackCommand() {
                    @Override
                    public void perform(@NonNull MediaControllerCompat.Callback callback) {
                        callback.onPlaybackStateChanged(state);
                    }
                });
            }

            // This might happen if the MusicService is killed while the Activity is in the
            // foreground and onStart() has been called (but not onStop()).
            @Override
            public void onSessionDestroyed() {
                Log.e(TAG, "세션끝남");
                resetState();
                Log.e(TAG, "상태 리셋");
                onPlaybackStateChanged(null);

                MediaBrowserHelper.this.onDisconnected();
            }
        }
    }