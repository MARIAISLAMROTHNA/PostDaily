package com.example.postdaily;

public class Posts {
    public String uid,email,pDescr,pId,pImage,pTime,name,image;

   public Posts(){

   }
    public Posts(String uid, String email, String pDescr, String pId, String pImage, String pTime, String name,String image) {
        this.uid = uid;
        this.email = email;
        this.pDescr = pDescr;
        this.pId = pId;
        this.pImage = pImage;
        this.pTime = pTime;
        this.name = name;
        this.image=image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getpDescr() {
        return pDescr;
    }

    public void setpDescr(String pDescr) {
        this.pDescr = pDescr;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpImage() {
        return pImage;
    }

    public void setpImage(String pImage) {
        this.pImage = pImage;
    }

    public String getimage() { return image; }

    public void setimage(String image) {
        this.image = image;
    }
    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
