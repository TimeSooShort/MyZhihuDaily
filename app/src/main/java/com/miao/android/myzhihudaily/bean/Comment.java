package com.miao.android.myzhihudaily.bean;

/**
 * Created by Administrator on 2016/9/4.
 */
public class Comment {

    private String avatarUrl;
    private String author;
    private String comment;
    private String time;

    public Comment(String avatarUrl,String author,String comment,String time) {
        this.avatarUrl = avatarUrl;
        this.author = author;
        this.comment = comment;
        this.time = time;
    }

    public String getAuthor() {
        return author;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getComment() {
        return comment;
    }

    public String getTime() {
        return time;
    }
}
