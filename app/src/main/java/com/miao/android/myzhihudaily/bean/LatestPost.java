package com.miao.android.myzhihudaily.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/28.
 */
public class LatestPost {

    private String title;
    private List<String> images = new ArrayList<String>();
    private String type;
    private String id;

    public LatestPost(String title, List<String> images, String type, String id) {
        this.title = title;
        this.images = images;
        this.type = type;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<String> getImages() {
        return images;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getFirstImg() {
        if (images.isEmpty())
            return null;
        return images.get(0);
    }
}
