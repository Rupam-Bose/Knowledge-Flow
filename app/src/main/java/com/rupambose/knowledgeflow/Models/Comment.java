package com.rupambose.knowledgeflow.Models;

public class Comment {
    public String id;
    public String text;
    public String uid;
    public String userName;
    public String profilePic;
    public long timestamp;
    public String date;
    public int likesCount;
    public boolean liked;

    public Comment() {
        // required for Firebase
    }
}

