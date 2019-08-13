package com.example.raelee.moodyj;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

public class AudioServiceInterface {

    private static final String TAG = "AudioServiceInterface";
    private ServiceConnection mConnection;
    private RecomMusicService mService;

    public AudioServiceInterface(Context context) {
        mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = ((RecomMusicService.RecomServiceBinder)service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mConnection = null;
                mService = null;

            }
        };

        context.bindService(new Intent(context, RecomMusicService.class)
        .setPackage(context.getPackageName()), mConnection, Context.BIND_AUTO_CREATE);

    }


    public void setPlayList(ArrayList<RecommendedLists> arrayList) {
        if (mService != null) {
            mService.setPlayList(arrayList);
        }
    }

    public RecommendedLists getAudioInfo(int position) {

        if (mService != null) {
            return mService.getAudioInfo(position);
        } return null;

    }

    public void play(int position) {
        if (mService != null) {
            mService.play(position);
        }
    }

    public void play() {
        if (mService != null) {
            mService.play();
        }
    }

    public void pause() {
        if(mService != null) {
            mService.pause();
        }
    }

    public void forward() {
        if (mService != null) {
            mService.forward();
        }
    }

    public void prev() {
        if (mService != null) {
            mService.prev();
        }
    }

    public void togglePlay() {
        if(isPlaying()) {
            mService.pause();
        } else {
            mService.play();
        }
    }

    public boolean isPlaying() {
        if (mService != null) {
            return mService.isPlaying();
        }
        return false;
    }

    public boolean isLooping() {
        if (mService != null) {
            return mService.isLooping();
        } return false;
    }

}
