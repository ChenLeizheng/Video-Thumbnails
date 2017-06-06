package com.self.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.self.myapplication.javabean.VideoBean;
import com.self.myapplication.view.XMediaController;

import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.VideoView;

/**
 * Created by lei on 2017/2/23.
 */
public class VideoPlayerActivity extends Activity{

    private XMediaController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        //使用全屏模式
        hideSystemUI();

        //第三方框架的初始化操作
        Vitamio.isInitialized(this);

        Intent intent = getIntent();
        VideoBean videoBean = (VideoBean) intent.getSerializableExtra("videoBean");
//        Log.i("test", "videoBean:" + videoBean);

        controller = (XMediaController) findViewById(R.id.controller);
        VideoView vv = (VideoView) findViewById(R.id.vv);
        controller.setDate(vv, videoBean);

//     获取视频地址   播放视频  SurfaceView + MediaPlayer = VideoView
//        vv.setVideoPath(videoBean.getPath());
//        vv.start();

        controller.setListener(new XMediaController.ExitClickListener() {
            //退出
            @Override
            public void onExit() {
                finish();
            }
            //上一首
            @Override
            public void onPrev(String prev) {
                Intent data = new Intent();
                data.setAction(prev);
//                data.putExtra("result",prev);
                //传递数据给打开自己的界面
                setResult(520, data);
                finish();
            }
            //下一首
            @Override
            public void onNext(String next) {
                Intent data = new Intent();
                data.setAction(next);
                setResult(520, data);
                finish();
            }
        });
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                // remove the following flag for version < API 19
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        controller.onPause();
        Log.d("test", "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        controller.onResume();
        Log.d("test", "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("test", "onDestroy");
    }
}
