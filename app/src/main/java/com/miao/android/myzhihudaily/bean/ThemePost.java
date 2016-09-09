package com.miao.android.myzhihudaily.bean;

/**
 * Created by Administrator on 2016/9/7.
 */
public class ThemePost {

    private String id;
    private String[] images;
    private String title;

    public ThemePost(String id, String[] image, String title) {
        this.id = id;
        this.images = image;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String[] getImages() {
        if (images == null)
            return null;
        return images;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstImg(){
        if (images == null)
            return null;
        return images[0];
    }
}
