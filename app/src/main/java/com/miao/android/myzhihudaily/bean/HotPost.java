package com.miao.android.myzhihudaily.bean;

/**
 * Created by Administrator on 2016/9/9.
 */
public class HotPost {
    private String news_id;
    private String url;
    private String title;
    private String thumbnail;

    public HotPost(String news_id, String url, String title, String thumbnail) {
        this.news_id = news_id;
        this.url = url;
        this.title = title;
        this.thumbnail = thumbnail;
    }

    public String getNews_id() {
        return news_id;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}
