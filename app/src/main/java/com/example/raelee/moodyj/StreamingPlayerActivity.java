package com.example.raelee.moodyj;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashChunkSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.pedro.encoder.input.video.CameraOpenException;


public class StreamingPlayerActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "StreamingPlayerActivity";

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private SimpleExoPlayer player;
    private PlayerView playerView;

    // icons 변수 선언
    private TextView playIng, playPause, closeStream;

    //private ComponentListener componentListener;

    private long playbackPosition;
    private int currentWindow;
    private boolean playWhenReady = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 타이틀바 삭제
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // notification bar 삭제
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // title/notification bar 삭제 후 레이아웃 띄우기
        setContentView(R.layout.activity_streaming_player);

        // FontAwesome 서체를 사용하여 아이콘 배치
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);


        // Create Simple Exoplayer Player

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        // componentListener = new ComponentListener();
        playerView = findViewById(R.id.simple_player);
        playerView.setPlayer(player);


        // Find Views by ID
        playIng = findViewById(R.id.playIng);
        playPause = findViewById(R.id.playPause);
        closeStream = findViewById(R.id.closeStream);

        // Set onClick Listeners
        playIng.setOnClickListener(this);
        playPause.setOnClickListener(this);
        closeStream.setOnClickListener(this);

        // Create RTMP Data Source
        RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
        MediaSource mediaSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory)
            .createMediaSource(Uri.parse("rtmp://54.180.86.123:1935/live"));

        player.prepare(mediaSource);
        player.setPlayWhenReady(true);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playPause:
                player.stop();
                if (playIng.getVisibility() == View.INVISIBLE) {
                    playIng.setVisibility(View.VISIBLE);
                    playPause.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.playIng:
                RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
                MediaSource mediaSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory)
                        .createMediaSource(Uri.parse("rtmp://54.180.86.123:1935/live"));

                player.prepare(mediaSource);
                player.setPlayWhenReady(true);

                     if (playPause.getVisibility() == View.INVISIBLE) {
                        playPause.setVisibility(View.VISIBLE);
                        playIng.setVisibility(View.INVISIBLE);
                    }
                    else {
                        Toast.makeText(this, "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT).show();
                    }

                break;
            case R.id.closeStream:
                try {
                    player.stop();
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
