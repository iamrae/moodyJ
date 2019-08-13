package com.example.raelee.moodyj;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MoodMainActivity extends AppCompatActivity {

    private static final String TAG = "MoodMainActivity";
    Context context;
    String openJson;
    Button button;
    SeekBar seekHappy, seekSensual, seekTempo;
    String title, artist, songId;

    ArrayList<SortedMusicList> list = new ArrayList<>();

    // 리사이클러뷰에 추려진 곡 목록 띄우기
    RecyclerView mRecyclerView;
    SortedRecyclerViewAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    // mediaBrowser 미리 연결해두기
    private MediaBrowserHelper mMediaBrowserHelper;
    boolean mIsPlaying;

    long sortedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "onCreate : Activity Started");
        // 타이틀바 삭제
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // notification bar 삭제
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // title/notification bar 삭제 후 레이아웃 띄우기
        this.setContentView(R.layout.activity_mood_main);

        // FontAwesome 서체를 사용하여 아이콘 배치
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);


        // 음악 분석하기 >> DB에 저장하기
        desrciptorToDB();

        // 무드에 따라 seekBar 조정하기
        seekHappy = findViewById(R.id.FunnyGloomy_bar);
        seekSensual = findViewById(R.id.SoftRough_bar);
        seekTempo = findViewById(R.id.Tempo_bar);

        seekHappy.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.getProgress();
                long progress = seekBar.getProgress();
                Log.e(TAG, "SeekHappy/onStopTrackingTouch: " + progress);
            }
        });
        seekSensual.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                seekBar.getProgress();
                long progress = seekBar.getProgress();
                Log.e(TAG, "SeekSentiment/onStopTrackingTouch: " + progress);

            }
        });
        seekTempo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                seekBar.getProgress();
                long progress = seekBar.getProgress();
                Log.e(TAG, "SeekTempo/onStopTrackingTouch: " + progress);

            }
        });

        // seekBar 조정 후 sorting 실행하는 버튼
        button = findViewById(R.id.buttonSort);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // seekBar 조정하고 버튼 클릭하면 분석값 받아오기

                DBHelper dbHelper = new DBHelper(context, "MusicDescriptorTable.db", null, 1);

                long happy = seekHappy.getProgress();
                long sensual = seekSensual.getProgress();
                long tempo = seekTempo.getProgress();

                context = getApplicationContext();

                Log.e(TAG, "onClick: get progress " + happy + " " + sensual + " " + tempo );
                dbHelper.sortByKeyScale(context, happy, sensual, tempo);
                //dbHelper.getResult();
                //Log.e(TAG, "check if Data is properly inserted " + dbHelper.getResult());


                // recyclerView (곡 목록) 관련
                mRecyclerView = findViewById(R.id.recycler_view);
                list = dbHelper.sortedMusicList;
                mAdapter = new SortedRecyclerViewAdapter(context, list);
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(context));



                // 곡분석 완료 후 추려진 곡을 리사이클러뷰에 띄우기
                mAdapter.setOnItemClickListener(new SortedRecyclerViewAdapter.OnItemClickListener() { // 어댑터에 온아이템클릭리스너 생성
                    @Override
                    public void onItemClick(int position) {

                        // 선택된 곡의 제목, 아티스트명, 곡의 ID 값
                        title = list.get(position).getTitle();
                        artist = list.get(position).getArtist();
                        songId = list.get(position).getSongId();

                        getSortedList();
                        //replacePosition(songId);

                        // 재생목록에 있는 곡을 누르면, 뮤직플레이어 액티비티로 이동하여 해당 곡을 재생하게 하는 인텐트
                        // 전달 값 : 노래 제목, 아티스트명, 곡 ID, ArrayList 에서의 위치값
                        Intent onclick = new Intent(MoodMainActivity.this, PlayerActivity.class);
                        // onclick.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        onclick.putExtra("title", title);
                        onclick.putExtra("artist", artist);
                        onclick.putExtra("songID", songId);
                        //onclick.putExtra("position", position);


                        startActivity(onclick);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                            //mMediaBrowserHelper.getTransportControls().skipToQueueItem((long)position);
                            mMediaBrowserHelper.getTransportControls().skipToQueueItem(sortedPosition);
                            Log.e(TAG, "OnItemClickListener skipToQueueItem: "+ sortedPosition);
                            mMediaBrowserHelper.getTransportControls().play();

                        }

                    }

                });
            }
        });

        // 미디어 브라우저 생성하고 연결해두기
        mMediaBrowserHelper = new MoodMainActivity.MediaBrowserConnection(this);
        mMediaBrowserHelper.registerCallback(new MoodMainActivity.MediaBrowserListener());
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = context.getAssets().open("f080.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    // 분석된 json 파일을 읽어오는 메소드
    public String loadJSONFromAssetLoop(String fileName) {
        String json = null;
        String f = fileName;
        //Log.e(TAG, "loadJSONFromAssetLoop" + fileName);
        try {
            InputStream is = context.getAssets().open(f);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    // 곡 150개의 값을 전부 표준화시키는 메소드 >> 곡분류 후에 표준화하는 방향으로 바꿈
    public void normalize(){
        context = getApplicationContext();
        DBHelper dbHelper = new DBHelper(context, "MusicDescriptorTable.db", null, 1);

        // 분석값이 저장된 DB 에서 각 항목의 최대값과 최소값을 가져온다.
        double alMax, alMin, dcMax, dcMin, bpmMax, bpmMin, danMax, danMin, ccrMax, ccrMin, cnrMax, cnrMin;

        alMax = dbHelper.getALMax(); // average Loudness
        alMin = dbHelper.getALMin();
        dcMax = dbHelper.getDCMax(); // dynamic Complexity
        dcMin = dbHelper.getDCMin();
        bpmMax = dbHelper.getBpmMax(); // bpm
        bpmMin = dbHelper.getBpmMin();
        danMax = dbHelper.getDanMax(); // danceability
        danMin = dbHelper.getDanMin();
        ccrMax = dbHelper.getCcrMax(); // chords Changes Rate
        ccrMin = dbHelper.getCcrMin();
        cnrMax = dbHelper.getCnrMax(); // chords Numbers Rate
        cnrMin = dbHelper.getCnrMin();


        // Normalize 계산

        int id = 1;
        NormDBHelper normDBHelper = new NormDBHelper(context, "MusicDescriptorNorm.db", null, 1);

        for (int i = 1; i<=150; i++) {

            id = i;

            // 1. average loudness
            double normAL, rawAL;
            rawAL = dbHelper.getAverageLoudness(id);
            normAL = ((rawAL - alMin) / (alMax - alMin));

            // 2. dynamic complexity
            double normDC, rawDC;
            rawDC = dbHelper.getDynamicComplexity(id);
            normDC = ((rawDC - dcMin) / (dcMax - dcMin));

            // 3. bpm
            double normBPM, rawBPM;
            rawBPM = dbHelper.getBpm(id);
            normBPM = ((rawBPM - bpmMin) / (bpmMax - bpmMin));

            // 4. danceability
            double normDan, rawDan;
            rawDan = dbHelper.getDanceability(id);
            normDan = ((rawDan - danMin) / (danMax - danMin));

            // 5. chords changes rate
            double normCcr, rawCcr;
            rawCcr = dbHelper.getChordsChangesRate(id);
            normCcr = ((rawCcr - ccrMin) / (ccrMax - ccrMin));

            // 6. chords numbers rate
            double normCnr, rawCnr;
            rawCnr = dbHelper.getChordsNumbersRate(id);
            normCnr = ((rawCnr - cnrMin) / (cnrMax - cnrMin));

            // 7. chords key
            String key, scale, title, artist;
            key = dbHelper.getKey(id);
            scale = dbHelper.getScale(id);
            title = dbHelper.getTitle(id);
            artist = dbHelper.getArtist(id);


/*
            // Insert Normalized Values into DB
            normDBHelper.insert(normAL, normDC, normBPM, normDan,
                    normCcr, normCnr, key, scale, title, artist);

*/

        }

        // DB에 몇개의 항목이 저장되었는지 db.getCount() 로 확인
        normDBHelper.getNumber();
        Log.e(TAG, "get the number of the inserted values " + normDBHelper.getNumber());

        //Log.e(TAG, "check it Data is properly inserted " + normDBHelper.getResult());
    }

    // 분석된 json 파일을 불러와 DB에 저장하는 메소드
    public void desrciptorToDB() {

        context = getApplicationContext();

        int fileNum = 1;
        // MusicDescriptor 에 있는 파일들을 DB에 담기
        DBHelper dbHelper = new DBHelper(context, "MusicDescriptorTable.db", null, 1);

        for (int i = 1; i <= 150; i++) {

            fileNum = i;
            String fileName = "f" + String.format("%03d",fileNum) + ".json";
            openJson = loadJSONFromAssetLoop(fileName);

            MusicDescriptorData musicData = new MusicDescriptorData();
            Gson gson = new Gson();
            musicData = gson.fromJson(openJson, MusicDescriptorData.class);

            // Log.e(TAG, "gson/jsonTest : " + musicData.getMetadata().getTags().getArtist());

            double averageLoudness = musicData.getLowlevel().getAverage_loudness();
            double dynamicComplexity = musicData.getLowlevel().getDynamic_complexity();
            double bpm = musicData.getRhythm().getBpm();
            double danceability = musicData.getRhythm().getDanceability();
            double chordsChangesRate = musicData.getTonal().getChords_changes_rate();
            double chordsNumbersRate = musicData.getTonal().getChords_number_rate();
            String chordsKey = musicData.getTonal().getChords_key();
            String chordsScale = musicData.getTonal().getChords_scale();
            String title = String.valueOf(musicData.getMetadata().getTags().getTitle());
            title = title.replaceAll("\\[(.*?)\\]", "$1"); // removing Brackets using RegEx
            String artist = String.valueOf(musicData.getMetadata().getTags().getArtist());
            artist = artist.replaceAll("\\[(.*?)\\]", "$1"); // removing Brackets using RegEx

            //Log.e(TAG, "dbHelper : " + chordsNumbersRate);


            /*
            // DB에 입력된 값 지우기
            dbHelper.delete(title);
            Log.e(TAG, "check it Data is properly deleted " + dbHelper.getResult());

            // DB에 값 입력하기
            dbHelper.insert(averageLoudness, dynamicComplexity, bpm, danceability,
                    chordsChangesRate, chordsNumbersRate, chordsKey, chordsScale, title, artist);

            Log.e(TAG, "check it Data is properly inserted " + dbHelper.getResult());*/

        }


        // Assets 폴더에 있는 분석 결과 파일 열기
        //openJson = loadJSONFromAsset();
        /*MusicDescriptorData musicData = new MusicDescriptorData();
        Gson gson = new Gson();
        musicData = gson.fromJson(openJson, MusicDescriptorData.class);

        Log.e(TAG, "gson/jsonTest : " + musicData.getMetadata().getTags().getArtist());

        // MusicDescriptor 에 있는 파일들을 DB에 담기
        DBHelper dbHelper = new DBHelper(context, "MusicDescriptorTable.db", null, 1);

        double averageLoudness = musicData.getLowlevel().getAverage_loudness();
        double dynamicComplexity = musicData.getLowlevel().getDynamic_complexity();
        double bpm = musicData.getRhythm().getBpm();
        double danceability = musicData.getRhythm().getDanceability();
        double chordsChangesRate = musicData.getTonal().getChords_changes_rate();
        double chordsNumbersRate = musicData.getTonal().getChords_number_rate();
        String chordsKey = musicData.getTonal().getChords_key();
        String chordsScale = musicData.getTonal().getChords_scale();
        String title = String.valueOf(musicData.getMetadata().getTags().getTitle());
        title = title.replaceAll("\\[(.*?)\\]", "$1"); // removing Brackets using RegEx
        String artist = String.valueOf(musicData.getMetadata().getTags().getArtist());
        artist = artist.replaceAll("\\[(.*?)\\]", "$1"); // removing Brackets using RegEx

        Log.e(TAG, "dbHelper : " + averageLoudness + " " + dynamicComplexity + " " + title + " " + artist);
        dbHelper.insert(averageLoudness, dynamicComplexity, bpm, danceability,
                chordsChangesRate, chordsNumbersRate, chordsKey, chordsScale, title, artist);

        Log.e(TAG, "check it Data is properly instered " + dbHelper.getResult());*/

    }


    // MediaBrowser Connection 관련

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

            // Queue up all media items for this simple sample.
            for (final MediaBrowserCompat.MediaItem mediaItem : children) {

                mediaController.addQueueItem(mediaItem.getDescription());
                Log.e(TAG, "MediaBrowserConnection : onChildrenLoaded + addQueueItem"+ mediaItem.getDescription());

            }

            for (int i = 0 ; i < list.size(); i++) {

                String title = list.get(i).getTitle();
                String artist = list.get(i).getArtist();
                String mediaId = list.get(i).getSongId();

            }
            mediaController.getQueue();
            Log.e(TAG, "MediaBrowserConnection : onChildrenLoaded _ getQueue() : " + mediaController.getQueue());
            // Call prepare now so pressing play just works.
            mediaController.getTransportControls().prepare();
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

    private long getSortedList(){

        for (int i = 0; i < mMediaBrowserHelper.getMediaController().getQueue().size(); i++) {

            String descTitle = mMediaBrowserHelper.getMediaController().getQueue().get(i).getDescription().getTitle().toString();

            // songId(int)와 memidId(string)을 비교해서 일치하면 일치한 값을 sortedPosition(long)에 담아서 재생시킨다
            if (title.equals(descTitle)) {

                sortedPosition = (long)i;
            }
        }
        return sortedPosition;
    }

}
