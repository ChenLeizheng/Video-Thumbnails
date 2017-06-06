package com.self.myapplication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.self.myapplication.javabean.VideoBean;

public class MainActivity extends Activity{

    private RecyclerView rv;
    private VideoAdapter adapter;
    private int position;
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SETTLING = 2;
    private LinearLayoutManager layoutManager;
    private int prePosition=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        rv = ((RecyclerView) findViewById(R.id.rv));
        //设置布局管理器
        layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        //设置适配器
        adapter = new VideoAdapter(this);
        rv.setAdapter(adapter);

        adapter.setOnItemClickLinstener(new VideoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(VideoBean videoBean,int position) {
                MainActivity.this.position = position;
                jumpToVideo(videoBean);
            }

        });

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                SharedPreferences sp = getSharedPreferences("flag",MODE_PRIVATE);
                switch (newState){
                    case SCROLL_STATE_DRAGGING:
                        sp.edit().putBoolean("isScrool",true).commit();

                        break;

                    //静止状态
                    case SCROLL_STATE_IDLE:
                        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                        sp.edit().putBoolean("isScrool",false).commit();
                        if (!rv.isComputingLayout() && prePosition!=firstVisibleItemPosition){
                            Log.d("MainActivity", "layoutManager.findFirstCompletelyVisibleItemPosition():" + layoutManager.findFirstCompletelyVisibleItemPosition()+" "+layoutManager.findFirstVisibleItemPosition());
                            adapter.notifyDataSetChanged();
                        }

                        prePosition = firstVisibleItemPosition;
//                        adapter.notifyDataSetChanged();
                        break;

                    case SCROLL_STATE_SETTLING:
                        sp.edit().putBoolean("isScrool",true).commit();
                        break;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    public interface onScrollStateChangedListener{
        void isScroll(boolean isScroll);
    }

    onScrollStateChangedListener listener;

    public void setListener(onScrollStateChangedListener listener) {
        this.listener = listener;
    }

    public void click(View v){
        VideoBean videoBean = new VideoBean();
        videoBean.setPath("http://vcntv.dnion.com/flash/mp4video58/TMS/2017/02/21/430bea9cdd8b41f882d30656cb8cc5be_h264418000nero_aac32.mp4");
        videoBean.setName("强哥访法");
        jumpToVideo(videoBean);
    }

    private void jumpToVideo(VideoBean videoBean) {
        //跳转到播放界面
        Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
        intent.putExtra("videoBean", videoBean);
//                Log.i("test", "MainActivity_videoBean:" + videoBean);
        startActivityForResult(intent, 22);
    }

    //关闭cursor
    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.recycle();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 520 && data!=null){
            String action = data.getAction();
            switch (action) {
                case "prev":
                    position --;
                    if (position < 0){
                        position = adapter.getItemCount()-1;
                    }
                    break;

                case "next":
                    position ++;
                    if (position>adapter.getItemCount()-1){
                        position = 0;
                    }
                    break;
            }

            VideoBean videoBean = adapter.getVideoBeanByCursorPosition(position);
            jumpToVideo(videoBean);
        }
    }
}
