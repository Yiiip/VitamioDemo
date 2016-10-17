package com.lyp.vitamiodemo;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

/**
 * Created by lyp on 2016/10/15.
 */
public class MyMediaController extends MediaController {

    private static final int HIDE_OP = 100;

    private ImageButton imgBack;        //返回键
    private TextView sysTime;           //系统时间
    private TextView sysBattery;        //电池电量
    private VideoView mVideoView;       //要控制的视频
    private Context mContext;
    private Activity mActivity;
    private GestureDetector mGestureDetector;
    private int controllerWidth = 0;    //设置mediaController高度为了使横屏时top显示在屏幕顶端


    private View mVolumeBrightnessLayout;
    private ImageView mOperationBg;
    private TextView mOperationTv;
    private AudioManager mAudioManager;
    private int mMaxVolume;             //最大声音
    private int mVolume = -1;           //当前声音
    private float mBrightness = -1f;    //当前亮度


    public MyMediaController(Context context, VideoView videoView , Activity activity) {
        super(context);
        this.mContext = context;
        this.mVideoView = videoView;
        this.mActivity = activity;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        controllerWidth = wm.getDefaultDisplay().getWidth();
        mGestureDetector = new GestureDetector(context, new MyGestureListener());
    }

    /**
     * 设置时间
     * @param time
     */
    public void setTime(String time){
        if (sysTime != null) {
            sysTime.setText(time);
        }
    }

    /**
     * 设置电量
     * @param stringBattery
     */
    public void setBattery(String stringBattery){
        if(sysTime != null && sysBattery != null){
            sysBattery.setText(stringBattery + "%");
        }
    }

    /**
     * 返回监听事件
     */
    private View.OnClickListener backListener = new View.OnClickListener() {
        public void onClick(View v) {
            if(mActivity != null){
                mActivity.finish();
            }
        }
    };

    /**
     * 重写控制器视图
     * @return
     */
    @Override
    protected View makeControllerView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.custom_video, this);
        view.setMinimumHeight(controllerWidth);

        imgBack = (ImageButton) view.findViewById(R.id.mediacontroller_top_back);
        sysBattery = (TextView) view.findViewById(R.id.mediacontroller_Battery);
        sysTime = (TextView) view.findViewById(R.id.mediacontroller_time);
        mVolumeBrightnessLayout = (RelativeLayout) view.findViewById(R.id.operation_volume_brightness);
        mOperationBg = (ImageView) view.findViewById(R.id.operation_bg);
        mOperationTv = (TextView) view.findViewById(R.id.operation_tv);
        mOperationTv.setVisibility(View.GONE);

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        imgBack.setOnClickListener(backListener);

        return view;
    }

    /**
     * 手势监听
     * 使用自定义的mediaController会铺满屏幕，所以VideoView的点击事件会被拦截，所以重写控制器的手势事件，将全部的操作全部写在控制器中
     */
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // 当手势结束，并且是单击结束时，隐藏或显示控制器
            toggleMediaControlsVisiblity();
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float mOldX = e1.getX();
            float mOldY = e1.getY();
            int y = (int) e2.getRawY();
            int x = (int) e2.getRawX();
            Display disp = mActivity.getWindowManager().getDefaultDisplay();
            int windowWidth = disp.getWidth();
            int windowHeight = disp.getHeight();

            if (mOldX > windowWidth * 3.0 / 4.0) { //屏幕右侧1/4，调整声音
                onVolumeSlide((mOldY - y) / windowHeight);
            } else if (mOldX < windowWidth * 1.0 / 4.0) { //屏幕右侧1/4，调整亮度
                onBrightnessSlide((mOldY - y) / windowHeight);
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // 双击暂停或开始播放
            playOrPause();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    /**
     * 隐藏或显示控制器
     */
    private void toggleMediaControlsVisiblity(){
        if (isShowing()) {
            hide();
        } else {
            show();
        }
    }

    /**
     * 播放与暂停
     */
    private void playOrPause(){
        if (mVideoView != null) {
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
            } else {
                mVideoView.start();
            }
        }
    }

    /**
     * 滑动改变声音大小
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        if (mVolume == -1) {
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0)
                mVolume = 0;
            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
            mOperationTv.setVisibility(VISIBLE);
        }

        int index = (int) (percent * mMaxVolume) + mVolume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;
        if (index >= 10) {
            mOperationBg.setImageResource(R.mipmap.volmn_100);
        } else if (index >= 5 && index < 10) {
            mOperationBg.setImageResource(R.mipmap.volmn_60);
        } else if (index > 0 && index < 5) {
            mOperationBg.setImageResource(R.mipmap.volmn_30);
        } else {
            mOperationBg.setImageResource(R.mipmap.volmn_no);
        }
        mOperationTv.setText((int) (((double) index / mMaxVolume)*100)+"%");
        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
    }


    /**
     * 滑动改变亮度
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (mBrightness < 0) {
            mBrightness = mActivity.getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f)
                mBrightness = 0.50f;
            if (mBrightness < 0.01f)
                mBrightness = 0.01f;

            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
            mOperationTv.setVisibility(VISIBLE);

        }
        WindowManager.LayoutParams lpa = mActivity.getWindow().getAttributes();
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        mActivity.getWindow().setAttributes(lpa);

        mOperationTv.setText((int) (lpa.screenBrightness * 100) + "%");

        if (lpa.screenBrightness * 100 >= 90) {
            mOperationBg.setImageResource(R.mipmap.light_100);
        } else if (lpa.screenBrightness * 100 >= 80 && lpa.screenBrightness * 100 < 90) {
            mOperationBg.setImageResource(R.mipmap.light_90);
        } else if (lpa.screenBrightness * 100 >= 70 && lpa.screenBrightness * 100 < 80) {
            mOperationBg.setImageResource(R.mipmap.light_80);
        } else if (lpa.screenBrightness * 100 >= 60 && lpa.screenBrightness * 100 < 70) {
            mOperationBg.setImageResource(R.mipmap.light_70);
        } else if (lpa.screenBrightness * 100 >= 50 && lpa.screenBrightness * 100 < 60) {
            mOperationBg.setImageResource(R.mipmap.light_60);
        } else if (lpa.screenBrightness * 100 >= 40 && lpa.screenBrightness * 100 < 50) {
            mOperationBg.setImageResource(R.mipmap.light_50);
        } else if (lpa.screenBrightness * 100 >= 30 && lpa.screenBrightness * 100 < 40) {
            mOperationBg.setImageResource(R.mipmap.light_40);
        } else if (lpa.screenBrightness * 100 >= 20 && lpa.screenBrightness * 100 < 20) {
            mOperationBg.setImageResource(R.mipmap.light_30);
        } else if (lpa.screenBrightness * 100 >= 10 && lpa.screenBrightness * 100 < 20) {
            mOperationBg.setImageResource(R.mipmap.light_20);
        }
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d("LOG", "MyMediaController-->dispatchKeyEvent()");
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        // 处理手势结束
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                endGesture();
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 手势结束
     */
    private void endGesture() {
        // 重置变量
        mVolume = -1;
        mBrightness = -1f;

        // 隐藏操作界面
        mHandler.removeMessages(HIDE_OP);
        mHandler.sendEmptyMessageDelayed(HIDE_OP, 100);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE_OP:
                    mVolumeBrightnessLayout.setVisibility(View.GONE);
                    mOperationTv.setVisibility(View.GONE);
                    break;
            }
        }
    };
}
