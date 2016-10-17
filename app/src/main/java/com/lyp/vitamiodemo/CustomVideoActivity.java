package com.lyp.vitamiodemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class CustomVideoActivity extends MainActivity implements Runnable {

    private static final int MSG_TIME = 0;
    private static final int MSG_BATTERY = 1;

    private VideoView mCustomVideoView;
    private MyMediaController myMediaController;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TIME:
                    myMediaController.setTime(msg.obj.toString());
                    break;
                case MSG_BATTERY:
                    myMediaController.setBattery(msg.obj.toString());
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //定义全屏参数，全屏将不显示系统状态栏
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //获得当前窗体对象
        Window window = CustomVideoActivity.this.getWindow();
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);

        setContentView(R.layout.activity_custom);

        mCustomVideoView = (VideoView) findViewById(R.id.customVideoView);

        myMediaController = new MyMediaController(this, mCustomVideoView, this);

        mCustomVideoView.setVideoPath(VIDEO_URL);
        mCustomVideoView.setMediaController(myMediaController);
        mCustomVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_LOW);//设置播放画质
        mCustomVideoView.requestFocus();//取得焦点

        registerBroadcastReceiver();
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (true) {
            // 获得系统时间
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            String time = format.format(new Date());

            Message msg = new Message();
            msg.obj = time;
            msg.what = MSG_TIME;
            mHandler.sendMessage(msg);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 广播接收器获取系统电量
     */
    private BroadcastReceiver batteryBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                // 获取当前电量
                int level = intent.getIntExtra("level", 0);
                // 电量的总刻度
                int scale = intent.getIntExtra("scale", 100);

                Message msg = new Message();
                msg.obj = (level*100)/scale;
                msg.what = MSG_BATTERY;
                mHandler.sendMessage(msg);
            }
        }
    };


    /**
     * 注册广播监听
     */
    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.batteryBroadcastReceiver);
    }
}
