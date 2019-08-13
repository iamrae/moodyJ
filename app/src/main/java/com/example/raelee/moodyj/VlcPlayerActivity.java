package com.example.raelee.moodyj;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.vlc.VlcListener;
import com.pedro.vlc.VlcVideoLibrary;

import java.util.Arrays;

public class VlcPlayerActivity extends AppCompatActivity implements VlcListener, View.OnClickListener{

    // VlcPlayer 가져오기
    private static final String TAG = "Pedro vlcPlayer";
    private VlcVideoLibrary vlcVideoLibrary;
    private String[] options = new String[]{":fullscreen"};

    // icons 변수 선언
    private TextView playIng, playPause, closeStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 타이틀바 삭제
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // notification bar 삭제
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // title/notification bar 삭제 후 레이아웃 띄우기
        this.setContentView(R.layout.activity_vlc_player);

        // FontAwesome 서체를 사용하여 아이콘 배치
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);

        SurfaceView surfaceView = findViewById(R.id.surfaceView);

        // Find Views by ID
        playIng = findViewById(R.id.playIng);
        playPause = findViewById(R.id.playPause);
        closeStream = findViewById(R.id.closeStream);

        vlcVideoLibrary = new VlcVideoLibrary(this, this, null, null);
        vlcVideoLibrary.setOptions(Arrays.asList(options));

        // Set onClick Listeners
        playIng.setOnClickListener(this);
        playPause.setOnClickListener(this);
        closeStream.setOnClickListener(this);

        // Audio Start streaming
        if (!vlcVideoLibrary.isPlaying()) {
            vlcVideoLibrary.play("rtmp://54.180.86.123:1935/live");
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playPause:
                vlcVideoLibrary.stop();
                if (playIng.getVisibility() == View.INVISIBLE) {
                    playIng.setVisibility(View.VISIBLE);
                    playPause.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.playIng:
                if (!vlcVideoLibrary.isPlaying()) {
                    vlcVideoLibrary.play("rtmp://54.180.86.123:1935/live");
                    if (playPause.getVisibility() == View.INVISIBLE) {
                        playPause.setVisibility(View.VISIBLE);
                        playIng.setVisibility(View.INVISIBLE);
                    }
                    else {
                        Toast.makeText(this, "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.closeStream:
                try {
                    vlcVideoLibrary.stop();
                    finish();
                } catch (CameraOpenException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void onComplete() {
        Toast.makeText(this, "Playing", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError() {
        Toast.makeText(this, "Error, make sure your endpoint is correct", Toast.LENGTH_SHORT).show();
        vlcVideoLibrary.stop();
    }
}
