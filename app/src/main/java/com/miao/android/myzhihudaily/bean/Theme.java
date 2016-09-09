package com.miao.android.myzhihudaily.bean;

/**
 * Created by Administrator on 2016/9/7.
 */
public class Theme {

    private String id;
    private String thumbnail;   //图片地址
    private String description;     //描述
    private String name;    //名称

    public Theme(String id, String thumbnail, String description, String name) {
        this.id = id;
        this.thumbnail = thumbnail;
        this.description =description;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }
}
