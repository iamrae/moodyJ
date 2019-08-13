package com.example.raelee.moodyj;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.example.raelee.moodyj.MusicLibrary;
import com.example.raelee.moodyj.MediaBrowserHelper;
import com.example.raelee.moodyj.MusicService;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // 권한 요청을 위한 임의의 숫자 지정
    static final int MY_PERMISSION_STORAGE = 1111;

    ImageView musicMood; // 무드 별 듣기, 내 음악 듣기, 스트리밍 하기
    View myMusic, musicStream;
    TextView welcomeMessage;

    public static MusicLibrary musicLibrary;
    public static ArrayList<MusicList> list = new ArrayList<>();

    private MediaBrowserHelper mMediaBrowserHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 타이틀바 삭제
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // notification bar 삭제
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        // title/notification bar 삭제 후 레이아웃 띄우기
        this.setContentView(R.layout.activity_main);

        // FontAwesome 서체를 사용하여 아이콘 배치
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);

        musicMood = findViewById(R.id.buttonMood);
        myMusic = findViewById(R.id.buttonList);
        musicStream = findViewById(R.id.buttonStream);
        welcomeMessage = findViewById(R.id.welcomeMessage);

        // 내 음악듣기 페이지로 이동하기
        myMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MyMusicListActivity.class); // 내 음악 목록으로 가기
                startActivity(intent);
            }
        });

        // 스트리밍 메인 페이지로 이동하기
        musicStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
                //Intent intent = new Intent(MainActivity.this, LogInActivity.class); // 내 음악 목록으로 가기
                //startActivity(intent);
            }
        });


        // 무드별 듣기 페이지로 이동하기
        musicMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MoodMainActivity.class); // 내 음악 목록으로 가기
                startActivity(intent);
            }
        });

        checkPermission();
        resolverToMetadata();
        getMusicList();


        mMediaBrowserHelper = new MediaBrowserConnection(this);
     //   mMediaBrowserHelper.registerCallback(new MediaBrowserListener());
        Log.e(TAG, "OnCreate 끝");


    }

    // 로그인이 필요합니다 메시지 띄우기
    private void showDialog() {

        Log.e(TAG, "showDialog 메소드 실행");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("로그인");
        builder.setMessage("로그인이 필요한 서비스입니다. 로그인하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, LogInActivity.class); // 로그인화면으로 이
                        startActivity(intent);
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMediaBrowserHelper.onStart();

        Intent welcome = getIntent();
        if (welcome.getStringExtra("email") != null) {
            welcomeMessage.setText(welcome.getStringExtra("email") + "님, 환영합니다!");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mMediaBrowserHelper.onStop();
        //mMediaBrowserHelper.getMediaController().removeQueueItem();
    }

    // 앱 처음 실행시 카메라, 외부저장소(읽기, 쓰기), 마이크 접근권한 요청하기
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 다시보지 않기 버튼을 만들려면 이 부분에 바로 요청을 하면 됨( 아래 else{ } 제거)
            // ActivityCompat.requestPermissions((Activity)mContext, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_CAMERA);

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setTitle("알림")
                        .setMessage("저장소 권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야 합니다.")
                        .setNeutralButton("설정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("Package:" + getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, MY_PERMISSION_STORAGE);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_STORAGE:
                for (int i = 0; i < grantResults.length; i++) {
                    // grantResults[] : 허용된 권한은 0, 거부한 권한은 -1
                    if (grantResults[i] < 0) {
                        Toast.makeText(MainActivity.this, "해당 권한을 허용해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                break;
        }
    }


    // 단말기에서 음악 가져오기
    public void getMusicList() {
        Log.e(TAG, "getting music from MediaStore."); // "로그 : mediaStore 에서 음악 가져오기"

        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME};

        String selection = MediaStore.Audio.Media.IS_MUSIC;
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);

        int pos =0;

        while (cursor.moveToNext()) {

            MusicList mList = new MusicList();
            // 단말기에 있는 음악 목록이 담기는 공간
            mList.setSongId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));// 곡의 아이디값
            //Log.e(TAG, "getting music from MediaStore."); // "로그 : mediaStore 에서 음악 가져오기"
            mList.setAlbumId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))); // 곡이 들어있는 앨범의 아이디값
            mList.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))); // 곡 제목
            mList.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))); // 아티스트
            mList.setDuration(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))); // 곡의 길이
            mList.setData(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
            mList.setDisplayName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
            pos++;

            //Log.e(TAG, "SONGID 담아라 " + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)) + " | " + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))+ " pos : " + pos);
            //데이터를 담는다.
            list.add(mList);

        }

        cursor.close();
        //Log.e(TAG, "Music from MediaStore is as follows:" + list);
    }

    // 단말기에서 음악 가져오기
    public void resolverToMetadata() {
        //Log.e(TAG, "getting music from MediaStore."); // "로그 : mediaStore 에서 음악 가져오기"

        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA, MediaStore.Audio.Albums._ID};

        String selection = MediaStore.Audio.Media.IS_MUSIC;
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);

        while (cursor.moveToNext()) {


            String mediaId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            long duration = Long.parseLong(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
            TimeUnit durationUnit = TimeUnit.SECONDS;
            String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
            String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            String albumId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums._ID));

            musicLibrary.createMediaMetadataCompat(mediaId, title, artist, album, duration, durationUnit, fileName, data, albumId);

        }

        cursor.close();
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
        protected void onConnected(@NonNull MediaControllerCompat mediaController) {
            Log.e(TAG, "MediaBrowserConnection : on Connected + mSeekBarAudio.setMediaController(controller)");
        }

        @Override
        protected void onChildrenLoaded(@NonNull String parentId,
                                        @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);

        }
    }

}