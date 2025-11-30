package com.appmsg.appmensajeriauem.model;

import org.bson.types.ObjectId;

public class User {
    private ObjectId id;
    private String password;
    private String picture;
    private String username;
    private String token;
    private String email;
    private String status;
    private String wallpaper;

    public User() {
    }

    public User(ObjectId id, String password, String picture, String username,
                String token, String email, String status, String wallpaper) {

        this.id = id;
        this.password = password;
        this.picture = picture;
        this.username = username;
        this.token = token;
        this.email = email;
        this.status = status;
        this.wallpaper = wallpaper;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWallpaper() {
        return wallpaper;
    }

    public void setWallpaper(String wallpaper) {
        this.wallpaper = wallpaper;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", password='" + password + '\'' +
                ", picture='" + picture + '\'' +
                ", username='" + username + '\'' +
                ", token='" + token + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                ", wallpaper='" + wallpaper + '\'' +
                '}';
    }
}
