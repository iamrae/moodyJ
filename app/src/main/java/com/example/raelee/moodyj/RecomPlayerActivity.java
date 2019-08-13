package com.example.raelee.moodyj;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class RecomPlayerActivity extends AppCompatActivity {

    private static final String TAG = "RecomPlayerActivity";

    String title, artist;
    TextView textTitle, textArtist, seekStart, seekEnd;
    ImageView albumArt;
    TextView playIng, playPause, playPrev, playNext;
    TextView playShuffle, playRepeat;

    public int position;

    private MediaSeekBar mSeekBar;
    private boolean mIsPlaying;
    private boolean mRepeating, mShuffling;

    private SharedPreferences preferences;


    // BroadcastReceiver
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            position = intent.getIntExtra("position", -1);
            int order = intent.getIntExtra("order", -33);
            Log.e(TAG, "onReceive: getPosition " + position + " from " + order);

            updateUI(position);

            /*
            경우의 수 3가지
            1. 곡 준비되면 오는 값(onPrepared에서 보내는 -> 1)
            2. 재생할 때 오는 값(play(i)에서 보내는 -> 3)
            3. 재생/일시정지 (값이 안와서 default로 -> -33)

            조건 별 상황
            A) 목록에서 putExtra 보내고 + onPrepared에서만 broadcast 보낸 경우 : 누른 곡은 제대로 나오는데 next/prev 할 때 계속 같은 곡만 나온다 (재생, 디스플레이 모두)
            B) 목록에서 putExtra 보내고 + onPrepared + play(i) 양쪽에서 broadcast 보낸 경우 : 누른 곡 제대로 나오고 next/prev 하면 재생되는 노래는 계속 바뀌는데 디스플레이가 안바뀜
            C) 목록에서 putExtra 안보내고 + onPrepared + play(i) 양쪽에서 broadcast 보낸 경우 : 뭘 누르든 0번 곡부터 재생, next/prev 컨트롤은 제대로 됨 (재생, 디스플레이 모두)
               + 대신에 다음 곡 재생할 때마다 toggle 버튼 깜빡임
            D) 목록에서 putExtra 안보내고 + onPrepared에서만 broadcast 보낸 경우 : 뭘 누르든 0번 화면이 나오고, 실제 누른 포지션의 곡이 재생되긴 한다, next/prev 컨트롤은 제대로 됨
               + 깜빡임 없음
            E) 목록에서 putExtra 안보내고 + play(i)에서만 broadcast 보낸 경우 : 뭘 누르든 늘 0번곡부터 재생, next/prev는 잘 돌아가는데 play/pause버튼이 제대로 작동하지 않음
               (재생됨과 동시에 pause버튼이 나와야하는데 play버튼부터 보임)
            F) 목록에서 putExtra 보내고 + play(i)에서만 broadcast 보낸 경우 : 누른대로 재생, play/pause버튼 작동 안함, next/prev할 때 곡은 바뀌는데 화면은 안바뀜 다음곡 한번만 유효

            결론
            목록에서 putExtra보내고 onPrepared/play(i)에서 모두 broadcast를 보내는데,
            받아온 포지션을 updateUI메소드의 파라미터로 넣어주어 혼선이 없도록 처리
            (getIntent().getExtras()떄문에 계속 이전 리스트에서 받아온 값을 적용하고 있었음)

            */
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 타이틀바 삭제
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // notification bar 삭제
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // title/notification bar 삭제 후 레이아웃 띄우기
        this.setContentView(R.layout.activity_recom_player);

        // FontAwesome 서체를 사용하여 아이콘 배치
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);

        // 재생 컨트롤 버튼
        playIng = findViewById(R.id.rec_playIng);
        playPause = findViewById(R.id.rec_playPause);
        playPrev = findViewById(R.id.rec_playPrev);
        playNext = findViewById(R.id.rec_playNext);
        playShuffle = findViewById(R.id.rec_playShuffle);
        playRepeat = findViewById(R.id.rec_playRepeat);


        // 곡 정보
        albumArt = findViewById(R.id.rec_view_albumArt);
        textTitle = findViewById(R.id.rec_view_title);
        textArtist = findViewById(R.id.rec_view_artist);
        mSeekBar = findViewById(R.id.rec_seekMusic);
        seekStart = findViewById(R.id.rec_seekStart);
        seekEnd = findViewById(R.id.rec_seekEnd);

        //mInterface = new AudioServiceInterface(getApplicationContext());
        registerBroadcast();
        updateUI(getIntent().getExtras().getInt("position"));

    }

    @Override
    protected void onStart(){
        super.onStart();

        // 재생 컨트롤 버튼에 온클릭 리스너 연결
        final RecomPlayerActivity.ClickListener clickListener = new RecomPlayerActivity.ClickListener();
        findViewById(R.id.rec_playIng).setOnClickListener(clickListener);
        findViewById(R.id.rec_playPause).setOnClickListener(clickListener);
        findViewById(R.id.rec_playPrev).setOnClickListener(clickListener);
        findViewById(R.id.rec_playNext).setOnClickListener(clickListener);
        findViewById(R.id.rec_playShuffle).setOnClickListener(clickListener);
        findViewById(R.id.rec_playRepeat).setOnClickListener(clickListener);
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
                case R.id.rec_playPrev:
                    //mMediaBrowserHelper.getTransportControls().skipToPrevious();
                    MoodyJ.getInstance().getServiceInterface().prev();
                    //updateUI();
                    /*
                    if (playPause.getVisibility() == View.INVISIBLE) {
                        playPause.setVisibility(View.VISIBLE);
                        playIng.setVisibility(View.INVISIBLE);
                    }*/

                    break;
                case R.id.rec_playPause:
                    //mMediaBrowserHelper.getTransportControls().pause();
                    MoodyJ.getInstance().getServiceInterface().togglePlay();
                    //updateUI();
                    /*
                    if (playIng.getVisibility() == View.INVISIBLE) {
                        playIng.setVisibility(View.VISIBLE);
                        playPause.setVisibility(View.INVISIBLE);
                    }*/

                    break;
                case R.id.rec_playIng:
                    //mMediaBrowserHelper.getTransportControls().play();
                    MoodyJ.getInstance().getServiceInterface().togglePlay();
                    //updateUI();
                    /*
                    if (playPause.getVisibility() == View.INVISIBLE) {
                        playPause.setVisibility(View.VISIBLE);
                        playIng.setVisibility(View.INVISIBLE);
                    }*/

                    break;
                case R.id.rec_playNext:
                    //mMediaBrowserHelper.getTransportControls().skipToNext();
                    MoodyJ.getInstance().getServiceInterface().forward();
                    //updateUI();
                    /*
                    if (playPause.getVisibility() == View.INVISIBLE) {
                        playPause.setVisibility(View.VISIBLE);
                        playIng.setVisibility(View.INVISIBLE);
                    }*/

                    break;
                case R.id.rec_playShuffle:

                    if (!mShuffling) {
                        Log.e(TAG, "Shuffle ON");
                        //mMediaBrowserHelper.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
                        playShuffle.setTextColor(Color.YELLOW);
                        mShuffling = true;

                    } else {
                        Log.e(TAG, "Shuffle OFF");
                        //mMediaBrowserHelper.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
                        playShuffle.setTextColor(Color.WHITE);
                        mShuffling = false;
                    }

                    break;

                case R.id.rec_playRepeat:
                    mRepeating = MoodyJ.getInstance().getServiceInterface().isLooping();
                    if (!mRepeating) {
                        Log.e(TAG, "Repetition ON");
                        //mMediaBrowserHelper.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
                        playRepeat.setTextColor(Color.YELLOW);
                        mRepeating = true;

                    } else {
                        Log.e(TAG, "Repetition OFF");
                        //mMediaBrowserHelper.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
                        playRepeat.setTextColor(Color.WHITE);
                        mRepeating = false;

                    }

                    break;
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
    }

    // BroadCast 관련 메소드
    private void registerBroadcast() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(RecomMusicService.BroadcastActions.PLAY_STATE_CHANGED);
        filter.addAction(RecomMusicService.BroadcastActions.PREPARED);
        registerReceiver(mReceiver, filter);

    }

    private void unregisterBroadcast() {

        unregisterReceiver(mReceiver);
    }

    private void updateUI(int i) {

        Log.e(TAG, "updateUI: " + position);

        // 재생 여부에 따라 버튼 스위치하기
        if (MoodyJ.getInstance().getServiceInterface().isPlaying()) {
            playPause.setVisibility(View.VISIBLE);
            playIng.setVisibility(View.INVISIBLE);
        } else {
            playIng.setVisibility(View.VISIBLE);
            playPause.setVisibility(View.INVISIBLE);
        }

        if (i != -1) {

            position = i;

            // 넘겨받은 포지션 값으로 곡 정보 업데이트하기
            String url = "http://ec2-13-125-241-11.ap-northeast-2.compute.amazonaws.com/Music/"; // 수정 서버(190718)
            String ext = ".jpg";

            RecommendedLists recommendedLists = MoodyJ.getInstance().getServiceInterface().getAudioInfo(position);
            Log.e(TAG, "updateUI: "+recommendedLists + " position: " + position);
            textTitle.setText(recommendedLists.getTitle());
            textArtist.setText(recommendedLists.getArtist());
            Glide.with(getApplicationContext())
                    .load(url+recommendedLists.getFileName()+ext).into(albumArt);

            }
    }

}
