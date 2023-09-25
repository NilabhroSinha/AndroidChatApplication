package com.example.chatapplication.model;

import java.util.List;

public class userModel {

    String userID, name, email, password, imageID, status, onlineStatus, birthday;
    Long timeStamp = 0L;
    List<String> friendsList;

    public userModel() {
    }

    public userModel(String userID, String name, String email, String password, String imageID, String status, List<String> friendsList, Long timeStamp, String onlineStatus, String birthday) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.password = password;
        this.imageID = imageID;
        this.status = status;;
        this.friendsList = friendsList;
        this.timeStamp = timeStamp;
        this.onlineStatus = onlineStatus;
        this.birthday = birthday;
    }

    public userModel(String userID, String email, String password, List<String> friendsList) {
        this.userID = userID;
        this.email = email;
        this.password = password;
        this.friendsList = friendsList;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getList() {
        return friendsList;
    }

    public void setList(List<String> friendsList) {
        this.friendsList = friendsList;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
