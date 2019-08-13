package com.example.raelee.moodyj;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;

/**
 * SeekBar that can be used with a {@link MediaSessionCompat} to track and seek in playing
 * media.
 */

public class MediaSeekBar extends AppCompatSeekBar {

    private static final String TAG = SeekBar.class.getSimpleName();

    private MediaControllerCompat mMediaController;
    private ControllerCallback mControllerCallback;

    private boolean mIsTracking = false;
    private OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.e(TAG, "onProgressChanged");

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.e(TAG, "onStartTrackingTouch");
            mIsTracking = true;
            //mMediaController.getTransportControls().pause();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.e(TAG, "onStopTrackingTouch");
            mMediaController.getTransportControls().seekTo(getProgress());
            Log.e(TAG, "onStopTrackingTouch" + getProgress());
            mIsTracking = false;
        }
    };

    private ValueAnimator mProgressAnimator;

    public MediaSeekBar(Context context) {
        super(context);
        super.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
    }

    public MediaSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
    }

    public MediaSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
    }

    @Override
    public final void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        // Prohibit adding seek listeners to this subclass.
        throw new UnsupportedOperationException("Cannot add listeners to a MediaSeekBar");
    }

    public void setMediaController(final MediaControllerCompat mediaController) {
        if (mediaController != null) {
            mControllerCallback = new ControllerCallback();
            mediaController.registerCallback(mControllerCallback);
        } else if (mMediaController != null) {
            mMediaController.unregisterCallback(mControllerCallback);
            mControllerCallback = null;
        }
        mMediaController = mediaController;
    }

    public void disconnectController() {
        if (mMediaController != null) {
            mMediaController.unregisterCallback(mControllerCallback);
            mControllerCallback = null;
            mMediaController = null;
        }
    }

    private class ControllerCallback
            extends MediaControllerCompat.Callback
            implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);

            // If there's an ongoing animation, stop it now.
            if (mProgressAnimator != null) {
                mProgressAnimator.cancel();
                mProgressAnimator = null;
            }

            //Log.e(TAG, "mProgressAnimator is running? " + mProgressAnimator.isRunning());
            final int progress = state != null
                    ? (int) state.getPosition()
                    : 0;
            setProgress(progress);
            Log.e(TAG, "setProgress" + progress + " mProgressAnimator " + mProgressAnimator);
            Log.e(TAG, "getState " + state.getState() + " PlaybackState " + PlaybackStateCompat.STATE_PLAYING);


            // If the media is playing then the seekbar should follow it, and the easiest
            // way to do that is to create a ValueAnimator to update it so the bar reaches
            // the end of the media the same time as playback gets there (or close enough).
            if (state != null && state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                final int timeToEnd = (int) ((getMax() - progress) / state.getPlaybackSpeed());

                mProgressAnimator = ValueAnimator.ofInt(progress, getMax())
                        .setDuration(timeToEnd);
                mProgressAnimator.setInterpolator(new LinearInterpolator());
                mProgressAnimator.addUpdateListener(this);
                mProgressAnimator.start();
                Log.e(TAG, "mProgressAnimator is running? " + mProgressAnimator.isRunning() + " is started? " + mProgressAnimator.isStarted());
                Log.e(TAG, "재생중일때 mProgressAnimator 시작되었는지"+ mProgressAnimator.getAnimatedValue() + " time to End :" + timeToEnd);
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);


            // If there's an ongoing animation, stop it now.
            if (mProgressAnimator != null) {
                mProgressAnimator.cancel();
                mProgressAnimator = null;
            }

            final int max = metadata != null
                    ? (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                    : 0;
            setProgress(0);
            setMax(max);
            Log.e(TAG, "setProgress & setMax "+ max);

/*
            // If the media is playing then the seekbar should follow it, and the easiest
            // way to do that is to create a ValueAnimator to update it so the bar reaches
            // the end of the media the same time as playback gets there (or close enough).


                mProgressAnimator = ValueAnimator.ofInt(0, getMax())
                        .setDuration(max);
                mProgressAnimator.setInterpolator(new LinearInterpolator());
                mProgressAnimator.addUpdateListener(this);
                mProgressAnimator.start();
                Log.e(TAG, "메타:mProgressAnimator is running? " + mProgressAnimator.isRunning() + " is started? " + mProgressAnimator.isStarted());
                Log.e(TAG, "메타:재생중일때 mProgressAnimator 시작되었는지"+ mProgressAnimator.getAnimatedValue() + " max :" + max);*/

        }

        @Override
        public void onAnimationUpdate(final ValueAnimator valueAnimator) {

            Log.e(TAG, "animation updated????");
            // If the user is changing the slider, cancel the animation.
            if (mIsTracking) {
                valueAnimator.cancel();
                return;
            }

            final int animatedIntValue = (int) valueAnimator.getAnimatedValue();
            setProgress(animatedIntValue);

            Log.e(TAG, "animatedIntValue: " + animatedIntValue);
        }

        public void progressAnimator(PlaybackStateCompat state){

            // If there's an ongoing animation, stop it now.
            if (mProgressAnimator != null) {
                mProgressAnimator.cancel();
                mProgressAnimator = null;
            }

            //Log.e(TAG, "mProgressAnimator is running? " + mProgressAnimator.isRunning());
            final int progress = state != null
                    ? (int) state.getPosition()
                    : 0;
            setProgress(progress);
            Log.e(TAG, "setProgress" + progress + " mProgressAnimator " + mProgressAnimator);
            Log.e(TAG, "getState " + state.getState() + " PlaybackState " + PlaybackStateCompat.STATE_PLAYING);


            // If the media is playing then the seekbar should follow it, and the easiest
            // way to do that is to create a ValueAnimator to update it so the bar reaches
            // the end of the media the same time as playback gets there (or close enough).
            if (state != null && state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                final int timeToEnd = (int) ((getMax() - progress) / state.getPlaybackSpeed());

                mProgressAnimator = ValueAnimator.ofInt(progress, getMax())
                        .setDuration(timeToEnd);
                mProgressAnimator.setInterpolator(new LinearInterpolator());
                mProgressAnimator.addUpdateListener(this);
                mProgressAnimator.start();
                Log.e(TAG, "mProgressAnimator is running? " + mProgressAnimator.isRunning() + " is started? " + mProgressAnimator.isStarted());
                Log.e(TAG, "재생중일때 mProgressAnimator 시작되었는지"+ mProgressAnimator.getAnimatedValue() + " time to End :" + timeToEnd);
            }
        }
    }


}