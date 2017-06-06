package com.self.myapplication;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.self.myapplication.javabean.VideoBean;
import com.self.myapplication.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by lei on 2017/2/22.
 */
public class VideoAdapter extends RecyclerView.Adapter {
    static Handler sHandler = new Handler();
    private Context context;
    private Map<String, String> map = new HashMap<>();
    private Cursor cursor;
    private MainActivity activity;
    private SharedPreferences sp;
    int i = 0;
    public VideoAdapter(Context context) {
        activity = (MainActivity) context;
        this.context = context;
        new FindVideoTask().execute();
        sp = context.getSharedPreferences("flag", context.MODE_PRIVATE);
//        setHasStableIds(true);
    }

//    @Override
//    public long getItemId(int position) {
//        return super.getItemId(position);
//    }

    public class FindVideoTask extends AsyncTask<Void, Void, Cursor> {
        //相当于是开启线程加载数据    子线程
        @Override
        protected Cursor doInBackground(Void... params) {
            ContentResolver contentResolver = context.getContentResolver();
            //获取视频的信息
            Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Video.VideoColumns._ID, MediaStore.Video.VideoColumns.DATA, MediaStore.Video.VideoColumns.DISPLAY_NAME, MediaStore.Video.VideoColumns.SIZE, MediaStore.Video.VideoColumns.DURATION},
                    null, null, null);

            //获取缩略图
            Cursor thumbCursor = contentResolver.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Video.Thumbnails.DATA, MediaStore.Video.Thumbnails.VIDEO_ID}, null, null, null);
            while (thumbCursor.moveToNext()) {
                //获取所有缩略图的路径
                String path = thumbCursor.getString(0);
                //获取所有缩略图对应的VideoID
                String videoId = thumbCursor.getString(1);
                //存入双列集合  键为videoID    将来可以直接通过videoID取到缩略图的路径
                map.put(videoId, path);
            }

            thumbCursor.close();

            return cursor;
        }

        //在任务执行后调用  3.主线程
        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            VideoAdapter.this.cursor = cursor;
            notifyDataSetChanged();
//            Log.i("test", "onPostExecute" + cursor);
        }
    }

    //创建ViewHolder的方法   加载item条目View
    //内部完成了convertView的复用   该方法只会执行 屏幕可显示的条目+1次
    //parent: 就是RecyclerView本身
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        VideoBean videoBean = new VideoBean();

        //加载条目布局
//        View itemView = View.inflate(context, R.layout.rv_video_list_item, null);

        // 告诉它它的父元素是谁 以便结合父节点算出孩子的大小否则layout的一些属性就无法生效   false不添加到parent 添加后返回到是RecyclerView
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View itemView = layoutInflater.inflate(R.layout.rv_video_list_item, parent, false);
        //创建ViewHolder   绑定ItemView和VideoBean对象(这时候VideoBean对象中的属性都没有赋值)
        VideoViewHolder videoViewHolder = new VideoViewHolder(itemView, videoBean);
        return videoViewHolder;
    }


    class VideoViewHolder extends RecyclerView.ViewHolder {
        VideoBean videoBean;
        ImageView ivIcon;
        TextView tvName;
        TextView tvSize;
        TextView tvDuration;

        public VideoViewHolder(View itemView, VideoBean videoBean) {
            super(itemView);
            this.videoBean = videoBean;

            ivIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvSize = (TextView) itemView.findViewById(R.id.tv_size);
            tvDuration = (TextView) itemView.findViewById(R.id.tv_duration);

        }

        public void bind(final VideoBean videoBean) {
            String path = videoBean.getPath();
            File file = new File(Environment.getExternalStorageDirectory(), md5(path) + ".png");
            if (file.exists()){
                Picasso.with(context).load(file).into(ivIcon);
            }else {
                ivIcon.setImageResource(R.mipmap.ic_launcher);
                boolean isScrool = sp.getBoolean("isScrool", false);
//            String s = ivIcon.getTag().toString();
                if (!isScrool){

                    Bitmap videoThumbnail = getVideoThumbnail(path);
                    if (videoThumbnail != null) {
                        ivIcon.setImageBitmap(videoThumbnail);
                    }
                }
            }


//            Picasso.with(context).
//            Picasso.with(context).load(videoBean.getPath()).into(ivIcon);
            tvName.setText(videoBean.getName());
            tvSize.setText(Formatter.formatFileSize(context, videoBean.getSize()));
            tvDuration.setText(Utils.formatDuration2HMS(videoBean.getDuration()));
        }

    }


    //绑定数据  holder   加载item条目时返回的ViewHolder
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final VideoViewHolder viewHolder = (VideoViewHolder) holder;

        //给itemview对应位置的videoBean赋值
        final VideoBean videoBean = getVideoBeanByCursorPosition(position);

        viewHolder.bind(videoBean);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(videoBean,position);
//                    Log.i("test", "listener+videoBean:" + videoBean);
                }
            }
        });
    }

    public interface OnItemClickListener{
        void onItemClick(VideoBean videoBean,int position);
    }

    private OnItemClickListener listener;

    public void setOnItemClickLinstener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 通过跳转cursor得到对应位置的JavaBean
     * @param position
     * @return
     */
    public VideoBean getVideoBeanByCursorPosition(int position) {
        cursor.moveToPosition(position);
        String id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID));
        String name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME));
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
        Long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION));
        Long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE));

        return new VideoBean(name, path, duration, size, id);
    }

    @Override
    public int getItemCount()  {
        return cursor == null ? 0 : cursor.getCount();
    }

    public void recycle() {
        if (cursor != null) {
            cursor.close();
        }
    }

    /**
     * 通过视频地址获取缩略图   显示出来
     * @param filePath
     * @return
     */
    public Bitmap getVideoThumbnail(final String filePath) {
        final Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MINI_KIND);
        Log.d("VideoAdapter", "bitmap.getWidth():" + bitmap.getWidth()+"  "+bitmap.getHeight());
        new Thread(new Runnable() {
            @Override
            public void run() {
                saveBitmap(bitmap,md5(filePath)+".png");
            }
        }).start();

        return bitmap;
    }

    //存缓存图
    public void saveBitmap(Bitmap bm,String picName) {
        File f = new File(Environment.getExternalStorageDirectory(),picName);
        if (f.exists()) {
            return;
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 56, out);
            Log.d("VideoAdapter", "ok"+f.toString());
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    static java.security.MessageDigest md;

    static {
        try {
            md = java.security.MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String md5(String md5) {
        byte[] array = md.digest(md5.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }
}

    /**
     * 通过视频地址获取缩略图   显示出来
     * @param filePath
     * @return
     */
/*    public Bitmap getVideoThumbnail(final String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);

            bitmap = retriever.getFrameAtTime();
//            Log.i("test",bitmap.getHeight()+"");

            //等比例缩小图片
            Matrix matrix = new Matrix();
            if (bitmap.getHeight()>1000) {
                matrix.postScale(0.1f, 0.1f); //长和宽放大缩小的比例
            }else {
                matrix.postScale(0.2f, 0.2f);
            }
            bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
//            Log.i("test",bitmap.getHeight()+"");


        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }
        finally {
            try {
                retriever.release();
            }
            catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }*/
