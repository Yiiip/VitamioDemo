package com.lyp.vitamiodemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

/**
 * https://github.com/Yiiip/VitamioDemo
 */
public class MainActivity extends AppCompatActivity {

    protected final static String VIDEO_URL = "http://media.neusoft.edu.cn/video/2016DNUI.mp4";

    private Button btn;
    private ImageView imgPlay;
    private RelativeLayout parentLayout;

    private VideoView mVideoView;
    private MediaController mMediaController;

    private boolean isInitialized = false;


    private void initVideo() {
        mMediaController = new MediaController(this);   //实例化控制器
        mMediaController.show(5000);                    //控制器显示5s后自动隐藏

        mVideoView.setVideoPath(VIDEO_URL);
        mVideoView.setMediaController(mMediaController);            //设置媒体控制器
        mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_LOW);   //设置播放画质
        mVideoView.requestFocus(); //取得焦点
        mVideoView.start();

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) { // 视频播放完
//                mp.seekTo(0);
                mp.stop();
                imgPlay.setVisibility(View.VISIBLE);
            }
        });
        mVideoView.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                if (!mVideoView.isPlaying()) {
                    imgPlay.setVisibility(View.INVISIBLE);
                }
            }
        });

        this.isInitialized = true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Vitamio.isInitialized(this); //初始化Vitamio

        btn = (Button) findViewById(R.id.btn);
        imgPlay = (ImageView) findViewById(R.id.imgPlay);
        parentLayout = (RelativeLayout) findViewById(R.id.videoParent);
        mVideoView = (VideoView) findViewById(R.id.videoView);


        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isInitialized) {
                    initVideo();
                } else {
                    mVideoView.start();
                }
                imgPlay.setVisibility(View.GONE);
            }
        });

        parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    imgPlay.setVisibility(View.VISIBLE);
                }
            }
        });


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoView.stopPlayback();
                startActivity(new Intent(MainActivity.this, CustomVideoActivity.class));
            }
        });
    }
}
