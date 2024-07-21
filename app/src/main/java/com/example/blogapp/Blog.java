package com.example.blogapp;

public class Blog {
    private String blogid;
    private String imageUrl;
    private String category;
    private String title;
    private String date;
    private long views;
    private long Timestamp;
    private String userName;
    private String description;



    public Blog(String blogid,String imageUrl, String category, String title, String date, long views,long Timestamp, String userName, String description) {
        this.blogid=blogid;
        this.imageUrl = imageUrl;
        this.category = category;
        this.title = title;
        this.date = date;
        this.views = views;
        this.Timestamp=Timestamp;
        this.userName=userName;
        this.description = description;
    }

    public String getBlogid() {
        return blogid;
    }

    public long getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(long timestamp) {
        Timestamp = timestamp;
    }

    public void setBlogid(String blogid) {
        this.blogid = blogid;
    }

    public Blog() {
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
