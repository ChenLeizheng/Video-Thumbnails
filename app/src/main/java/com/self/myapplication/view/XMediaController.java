package com.self.myapplication.view;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.self.myapplication.R;
import com.self.myapplication.javabean.VideoBean;
import com.self.myapplication.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.VideoView;

/**
 * Created by lei on 2017/2/23.
 */
public class XMediaController extends LinearLayout implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener {

    private VideoView vv;
    private VideoBean videoBean;
    private Handler handler = new Handler();

    private LinearLayout llControllerTop;
    private TextView tvControllerTitle;
    private TextView tvControllerTime;
    private ImageView ivControllerBattery;
    private ImageView ivControllerMute;
    private SeekBar sbControllerVolume;
    private LinearLayout llControllerBottom;
    private TextView tvControllerPassedTime;
    private SeekBar sbControllerPosition;
    private TextView tvControllerTotalTime;
    private ImageView ivControllerExit;
    private ImageView ivControllerPrev;
    private ImageView ivControllerPlay;
    private ImageView ivControllerNext;
    private ImageView ivControllerFullScreen;
    private LinearLayout llControllerBufferingContainer;
    private LinearLayout llControllerLoadingContainer;
    private AudioManager am;

    public XMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.view_controller, this);

        init();

        initData();
    }

    private void initData() {
        am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        int streamMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolum = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        sbControllerVolume.setMax(streamMaxVolume);//设置sb的最大值

        //回显音量
        sbControllerVolume.setProgress(currentVolum);
        if (currentVolum == 0) {
            ivControllerMute.setImageResource(R.mipmap.volume_up);
        }
        sbControllerVolume.setOnSeekBarChangeListener(volumeChangedListener);

        ivControllerMute.setOnClickListener(muteChangedListener);//音量

        ivControllerPlay.setOnClickListener(playClickListener);//播放

        sbControllerPosition.setOnSeekBarChangeListener(playingPositionChangedListener); //播放进度条拖动

        ivControllerExit.setOnClickListener(exitClickListener);  //返回

        ivControllerFullScreen.setOnClickListener(fullScreenListener);  //全屏

        ivControllerPrev.setOnClickListener(prevOrNextClickListener); //上一个
        ivControllerNext.setOnClickListener(prevOrNextClickListener); //下一个
    }

    //    1.  事件传递(分发)	: dispatchTouchEvent
//    1.5 事件拦截		: onInterceptTouchEvent  不能拦截拦截了子元素seekbar就拖不了了
//    2.  事件处理		: onTouchEvent 在move的时候移除动画任务（seekbar拖动会出现到点隐藏）
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:

//                Log.d("test", "down");
                handler.removeCallbacks(showControllerRunnable);
                handler.removeCallbacks(hideControllerRunnable);

                handler.post(showControllerRunnable);
                handler.postDelayed(hideControllerRunnable, 2500);

                break;

            case MotionEvent.ACTION_MOVE:
//                Log.d("test", "ACTION_MOVE");
                handler.removeCallbacks(hideControllerRunnable);
                break;

            case MotionEvent.ACTION_UP:
//                Log.d("test", "ACTION_UP");
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    //显示隐藏的动画
    Runnable showControllerRunnable = new Runnable() {
        @Override
        public void run() {
            ObjectAnimator animator1 = ObjectAnimator.ofFloat(llControllerTop, "translationY", llControllerTop.getTranslationY(), 0);
            animator1.setDuration(500);
            animator1.start();
            ObjectAnimator animator2 = ObjectAnimator.ofFloat(llControllerBottom, "translationY", llControllerBottom.getTranslationY(), 0);
            animator2.setDuration(500);
            animator2.start();
        }
    };

    Runnable hideControllerRunnable = new Runnable() {
        @Override
        public void run() {
            ObjectAnimator animator1 = ObjectAnimator.ofFloat(llControllerTop, "translationY", 0, -llControllerTop.getHeight());
            animator1.setDuration(500);
            animator1.start();
            ObjectAnimator animator2 = ObjectAnimator.ofFloat(llControllerBottom, "translationY", 0, llControllerBottom.getHeight());
            animator2.setDuration(500);
            animator2.start();
        }
    };

    //---------------------上一首下一首监听--------------------------
    OnClickListener prevOrNextClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_controller_prev:
                    if (listener != null) {
                        listener.onPrev("prev");
                    }
                    break;

                case R.id.iv_controller_next:
                    if (listener != null) {
                        listener.onNext("next");
                    }
                    break;
            }
        }
    };

    //---------------------全屏--------------------------
    private boolean isFullScreen;
    OnClickListener fullScreenListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isFullScreen) {
                //复原  布局参数传空  按原来的布局来
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                vv.setLayoutParams(layoutParams);

            } else {
                //实现全屏
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                vv.setLayoutParams(layoutParams);
            }

            isFullScreen = !isFullScreen;
        }
    };

    //---------------------播放界面的返回键--------------------------
    private ExitClickListener listener;

    public void setListener(ExitClickListener listener) {
        this.listener = listener;
    }

    public interface ExitClickListener {
        void onExit();

        void onPrev(String prev);

        void onNext(String next);
    }


    OnClickListener exitClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onExit();
            }
        }
    };
    //------------------------播放-----------------------
    OnClickListener playClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (vv.isPlaying()) {
                //暂停
                vv.pause();
                ivControllerPlay.setImageResource(R.drawable.btn_play_selector);
            } else {
                //播放
                vv.start();
                ivControllerPlay.setImageResource(R.drawable.btn_pause_selector);

                Log.d("test", "up vv.getCurrentPosition():" + vv.getCurrentPosition());
                //当再次开始播放时  开启进度更新的任务
                handler.post(updatePlayingPositionRunnable);
                Log.d("test", "down vv.getCurrentPosition():" + vv.getCurrentPosition());
            }
        }
    };

    //---------------------静音--------------------------
    int preVolume;
    OnClickListener muteChangedListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (currentVolume == 0) {
                //此时为静音   点击后恢复
                am.setStreamVolume(AudioManager.STREAM_MUSIC, preVolume, 0);
                ivControllerMute.setImageResource(R.mipmap.volume_up);
            } else {
                //存储当前状态  然后设置为静音
                preVolume = currentVolume;
                am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                ivControllerMute.setImageResource(R.mipmap.volume_off);
            }
        }
    };

    //---------------------拖动sb改播放进度--------------------------
    boolean preIsPlaying;
    SeekBar.OnSeekBarChangeListener playingPositionChangedListener = new SeekBar.OnSeekBarChangeListener() {
        //参数3: 表示 是否为用户拖动的 而非代码改变的
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                vv.seekTo(progress);
                tvControllerPassedTime.setText(Utils.formatDuration2HMS(progress));
                Log.d("test", "onProgressChanged:" + vv.getCurrentPosition());
            }
        }

        //当手摸上去的时候
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //记录当前的播放状态
            preIsPlaying = vv.isPlaying();
            if (vv.isPlaying()) {
                vv.pause();
//                playClickListener.onClick(ivControllerPlay);
            }

            Log.d("test", "onStartTrackingTouch:" + vv.getCurrentPosition());
        }

        //当手离开的时候
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
          /*  if (preIsPlaying) {
                vv.start();
                handler.post(updatePlayingPositionRunnable);
//                playClickListener.onClick(ivControllerPlay);
            }*/
//            Log.d("test", "onStartTrackingTouch" + vv.isPlaying() + "---preIsPlaying" + preIsPlaying);
            vv.start();
            handler.post(updatePlayingPositionRunnable);
            if (!preIsPlaying){
                ivControllerPlay.setImageResource(R.drawable.btn_pause_selector);
            }
            Log.d("test", "onStopTrackingTouch:" + vv.getCurrentPosition());
        }
    };

    //---------------------拖动sb改音量--------------------------
    SeekBar.OnSeekBarChangeListener volumeChangedListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //falg AudioManager.FLAG_SHOW_UI 显示系统的调试音量   0 就是啥都不显示
            am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);

            //拖动到0时改变图标状态
            if (progress == 0) {
                ivControllerMute.setImageResource(R.mipmap.volume_off);
            } else {
                ivControllerMute.setImageResource(R.mipmap.volume_up);

            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void init() {
        llControllerTop = (LinearLayout) findViewById(R.id.ll_controller_top);
        tvControllerTitle = (TextView) findViewById(R.id.tv_controller_title);
        tvControllerTime = (TextView) findViewById(R.id.tv_controller_time);
        ivControllerBattery = (ImageView) findViewById(R.id.iv_controller_battery);
        ivControllerMute = (ImageView) findViewById(R.id.iv_controller_mute);
        sbControllerVolume = (SeekBar) findViewById(R.id.sb_controller_volume);
        llControllerBottom = (LinearLayout) findViewById(R.id.ll_controller_bottom);
        tvControllerPassedTime = (TextView) findViewById(R.id.tv_controller_passed_time);
        sbControllerPosition = (SeekBar) findViewById(R.id.sb_controller_position);
        tvControllerTotalTime = (TextView) findViewById(R.id.tv_controller_total_time);
        ivControllerExit = (ImageView) findViewById(R.id.iv_controller_exit);
        ivControllerPrev = (ImageView) findViewById(R.id.iv_controller_prev);
        ivControllerPlay = (ImageView) findViewById(R.id.iv_controller_play);
        ivControllerNext = (ImageView) findViewById(R.id.iv_controller_next);
        ivControllerFullScreen = (ImageView) findViewById(R.id.iv_controller_full_screen);
        llControllerBufferingContainer = (LinearLayout) findViewById(R.id.ll_controller_buffering_container);
        llControllerLoadingContainer = (LinearLayout) findViewById(R.id.ll_controller_loading_container);
    }

    public void setDate(VideoView vv, VideoBean videoBean) {
        this.vv = vv;
        this.videoBean = videoBean;
        vv.setVideoPath(videoBean.getPath());

        //设置准备完成的监听
        vv.setOnPreparedListener(this);

        //设置播放完成的监听
        vv.setOnCompletionListener(this);

        vv.setOnInfoListener(this);
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                llControllerBufferingContainer.setVisibility(View.VISIBLE);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                llControllerBufferingContainer.setVisibility(View.GONE);
                break;
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        ivControllerPlay.setImageResource(R.drawable.btn_play_selector);

        //强行设为总时间  完美的解决该Bug
        tvControllerPassedTime.setText(Utils.formatDuration2HMS(vv.getDuration()));
    }

    //---------------------videoView的生命周期   准备好了--------------------------
    @Override
    public void onPrepared(MediaPlayer mp) {
        //设置标题
        tvControllerTitle.setText(videoBean.getName());

        vv.start();

        //设置播放进度条的最大值
        sbControllerPosition.setMax((int) vv.getDuration());
//        Log.d("test", "vv.getDuration():" + vv.getDuration());

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String time = sdf.format(new Date());
        tvControllerTime.setText(time);
        tvControllerTotalTime.setText(Utils.formatDuration2HMS(vv.getDuration()));

        handler.post(updatePlayingPositionRunnable);  //动态更新播放进度

        handler.postDelayed(hideControllerRunnable, 2200);

        llControllerLoadingContainer.setVisibility(View.GONE);
    }

    //---------------------在VideoPlayActivity生命周期方法中调用，同步状态--------------------------
    public void onResume() {
        handler.post(updateTimeRunnable); //动态更新时间

        //注册广播  更新电量
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        getContext().registerReceiver(batteryRecever, filter);

        //注册广播  更新音量
        IntentFilter volumeFilter = new IntentFilter();
        volumeFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        getContext().registerReceiver(volumeReceiver, volumeFilter);
    }

    public void onPause() {
        //移除任务
        handler.removeCallbacks(updateTimeRunnable);
        handler.removeCallbacks(updatePlayingPositionRunnable);
        //注销广播
        getContext().unregisterReceiver(batteryRecever);
        getContext().unregisterReceiver(volumeReceiver);
    }


    //------------------------------------------顶部电量-------------------------------------------------
    int[] batteryIcons = new int[]{
            R.mipmap.ic_battery_0,
            R.mipmap.ic_battery_10,
            R.mipmap.ic_battery_20,
            R.mipmap.ic_battery_20,
            R.mipmap.ic_battery_40,
            R.mipmap.ic_battery_40,
            R.mipmap.ic_battery_60,
            R.mipmap.ic_battery_60,
            R.mipmap.ic_battery_80,
            R.mipmap.ic_battery_80,
            R.mipmap.ic_battery_100,
    };

    //---------------------同步系统音量--------------------------
    BroadcastReceiver volumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //接收音量广播  设置当前的音量更新sb的进度
            int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            sbControllerVolume.setProgress(currentVolume);
//            Log.i("test", "currentVolume:" + currentVolume);
        }
    };

    //---------------------同步系统电量--------------------------
    BroadcastReceiver batteryRecever = new BroadcastReceiver() {
        //接收到电量广播  设置当前电量为相应的图片
        @Override
        public void onReceive(Context context, Intent intent) {
            //level 当前电量   scale总电量
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
//          int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
            ivControllerBattery.setImageResource(batteryIcons[level / 10]);
//            Log.i("test", "level:" + level);
        }
    };

    //---------------------更新时间的任务--------------------------
    Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            //HH获取21：50   hh获取09：50
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            String time = sdf.format(new Date());
            tvControllerTime.setText(time);
            //子线程没走完就卡那了  没法更新tv  显示的结果就是00：00：00
            handler.postDelayed(this, 1000 * 30);
        }
    };
    //---------------------更新播放进度的任务--------------------------
    Runnable updatePlayingPositionRunnable = new Runnable() {
        @Override
        public void run() {
            int currentPosition = (int) vv.getCurrentPosition();
//            Log.d("getCurrentPosition", "vv.getCurrentPosition():" + vv.getCurrentPosition());
            sbControllerPosition.setProgress(currentPosition);

            //更新text的时间
            tvControllerPassedTime.setText(Utils.formatDuration2HMS(currentPosition));
//            Log.d("test", "currentPosition:" + currentPosition);
            //暂停的时候就不用更新进度了

            if (vv.isPlaying()) {
                handler.postDelayed(this, 100);
            }
        }
    };
}
