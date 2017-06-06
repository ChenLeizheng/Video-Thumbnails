package com.self.myapplication.javabean;

import java.io.Serializable;

/**
 * Created by lei on 2017/2/22.
 */
public class VideoBean  implements Serializable {
    private String name;
    private String path;
    private long duration;
    private long size;
    private String id;

    public VideoBean() {
    }

    public VideoBean(String name, String path, long duration, long size, String id) {
        this.name = name;
        this.path = path;
        this.duration = duration;
        this.size = size;
        this.id = id;
    }

    @Override
    public String toString() {
        return "VideoBean{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                ", id='" + id + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
