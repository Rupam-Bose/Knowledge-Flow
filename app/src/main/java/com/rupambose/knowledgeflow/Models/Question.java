package com.rupambose.knowledgeflow.Models;

import java.util.Map;

public class Question {
    public String questionId;
    public String questionText;
    public String uid;
    public String userName;
    public String profilePic;
    public long timestamp;
    public String date;
    public int likesCount;
    public int answersCount;
    public Map<String, Boolean> likes;
    public Map<String, Answer> answers;

    public Question() { }
}
