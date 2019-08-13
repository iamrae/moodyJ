package com.example.raelee.moodyj;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Telephony;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtplibrary.rtmp.RtmpCamera2;
import com.pedro.rtplibrary.rtmp.RtmpOnlyAudio;

import net.ossrs.rtmp.ConnectCheckerRtmp;

import java.io.File;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class StreamerActivity extends AppCompatActivity
        implements ConnectCheckerRtmp, SurfaceHolder.Callback, View.OnClickListener {

    private static final String TAG = "StreamerActivity";
    private RtmpCamera2 rtmpCamera2;
    //private RtmpOnlyAudio rtmpOnlyAudio;

    private TextView playIng, playPause, closeStream;

    //private Button bRecord;
    //private EditText etUrl;

    private String rtmpUrl;

    private String currentDateAndTime = "";
    private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/rtmp-rtsp-stream-client-java");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 타이틀바 삭제
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // notification bar 삭제
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // title/notification bar 삭제 후 레이아웃 띄우기
        this.setContentView(R.layout.activity_streamer_or);

        // FontAwesome 서체를 사용하여 아이콘 배치
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);

        SurfaceView surfaceView = findViewById(R.id.surfaceView);

        // Find Views by ID
        playIng = findViewById(R.id.playIng);
        playPause = findViewById(R.id.playPause);
        closeStream = findViewById(R.id.closeStream);

        // Audio Start Streaming
        rtmpUrl = "rtmp://54.180.86.123:1935/live";
        rtmpCamera2 = new RtmpCamera2(surfaceView, this);
        Log.e(TAG, "onCreate: new RtmpCamera2 - 1");
        surfaceView.getHolder().addCallback(this);
        playIng.setOnClickListener(this);
        Log.e(TAG, "onCreate: new RtmpCamera2 - 3");
        playPause.setOnClickListener(this);
        Log.e(TAG, "onCreate: new RtmpCamera2 - 4");
        closeStream.setOnClickListener(this);
        Log.e(TAG, "onCreate: new RtmpCamera2 - 5");


        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                int width = surfaceView.getWidth();

                    while (width == 0) {
                        Log.e(TAG, "width = "+ surfaceView.getWidth());
                        try {
                            Thread.sleep(1000);
                            width = surfaceView.getWidth();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                if(rtmpCamera2.prepareAudio() && rtmpCamera2.prepareVideo()) {
                    rtmpCamera2.startStream(rtmpUrl);
                }
            }
        });



        th.start();

        Log.e(TAG, "onCreate: new RtmpCamera2 - 2");
      /*  if (!rtmpCamera2.isStreaming()) {
            if(rtmpCamera2.prepareAudio() && rtmpCamera2.prepareVideo()){
                Log.e(TAG, "onCreate: !rtmpCamera2 is Streaming / prepareAudio+video");
                rtmpCamera2.startStream(rtmpUrl);
            } else {
                Toast.makeText(this, "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT).show();
            }
        }*/


        //rtmpOnlyAudio = new RtmpOnlyAudio(this);
        //rtmpOnlyAudio.startStream(rtmpUrl);

        //SurfaceView surfaceView = findViewById(R.id.surfaceView);
        //start_live = findViewById(R.id.b_start_stop);

        /**
         * 라이브 시작 버튼 클릭 리스너
         */

    }

    @Override
    protected void onStart() {
        super.onStart();
        //Log.e(TAG, "onStart: new RtmpCamera2 - 6 "+ rtmpUrl);
        //rtmpCamera2.startStream(rtmpUrl);
        //Log.e(TAG, "onStart: new RtmpCamera2 - 6");


    }


    /**
     * Surfaceview 메소드
     */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        rtmpCamera2.startPreview();
        Log.e(TAG, "onStart: new RtmpCamera2 - 7");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        /*if (rtmpCamera2.isRecording()) {
            rtmpCamera2.stopRecord();
            bRecord.setText("Start Record");
            Toast.makeText(this, "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            currentDateAndTime = "";
        }
        if (rtmpCamera2.isStreaming()) {
            rtmpCamera2.stopStream();
            start_live.setText("Start stream");
        }*/
        rtmpCamera2.stopPreview();
    }

    /**
     * RTMP 메소드
     */
    // 연결이 성공할 경우..
    @Override
    public void onConnectionSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(StreamerActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 연결이 실패할 경우...
    @Override
    public void onConnectionFailedRtmp(String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(StreamerActivity.this, "Connection Failed", Toast.LENGTH_SHORT).show();
                rtmpCamera2.stopStream();
                //rtmpOnlyAudio.stopStream();
                //start_live.setText("Start stream");
            }
        });
    }

    // 연결을 끊을 경우...
    @Override
    public void onDisconnectRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(StreamerActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAuthErrorRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(StreamerActivity.this, "Auth error", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onAuthSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(StreamerActivity.this, "Auth success", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playPause:
                rtmpCamera2.stopStream();
                //rtmpOnlyAudio.stopStream();
                if (playIng.getVisibility() == View.INVISIBLE) {
                    playIng.setVisibility(View.VISIBLE);
                    playPause.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.playIng:
                /*if (!rtmpOnlyAudio.isStreaming()) {
                    if (rtmpOnlyAudio.prepareAudio()) {
                        rtmpOnlyAudio = new RtmpOnlyAudio(this);
                        rtmpOnlyAudio.startStream(rtmpUrl);
                        if (playPause.getVisibility() == View.INVISIBLE) {
                            playPause.setVisibility(View.VISIBLE);
                            playIng.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        Toast.makeText(this, "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT).show();
                    }
                }*/
                if (!rtmpCamera2.isStreaming()) {
                    if (rtmpCamera2.isRecording() || rtmpCamera2.prepareAudio() && rtmpCamera2.prepareVideo()) {
                        rtmpCamera2.startStream(rtmpUrl);
                        if (playPause.getVisibility() == View.INVISIBLE) {
                            playPause.setVisibility(View.VISIBLE);
                            playIng.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        Toast.makeText(this, "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT).show();
                    }
                }
            break;
            case R.id.closeStream:
                try {
                    rtmpCamera2.stopStream();
                    //rtmpOnlyAudio.stopStream();
                        finish();
                } catch (CameraOpenException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

}
