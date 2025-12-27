package com.rupambose.knowledgeflow.Home.adapter;

public class blogItem {
    private String contentTitle;
    private String profilePic;
    private String profileName;
    private String date;
    private String post;
    private int likesCount;
    private int commentsCount;
    private boolean liked;

    // Required empty constructor for Firebase/serialization
    public blogItem() {
        this("", "", "", "", "", 0, 0);
    }

    public blogItem(String contentTitle,
                    String profilePic,
                    String profileName,
                    String date,
                    String post,
                    int likesCount,
                    int commentsCount) {
        this.contentTitle = contentTitle;
        this.profilePic = profilePic;
        this.profileName = profileName;
        this.date = date;
        this.post = post;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.liked = false;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
