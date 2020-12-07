package com.example.postdaily;

public class Comments {
    String comment,date,name,image,username,time;
    public Comments()
    {

    }
    public Comments(String comment, String date, String name, String image, String username, String time) {
        this.comment = comment;
        this.date = date;
        this.name = name;
        this.image = image;
        this.username = username;
        this.time=time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
