package com.appmsg.appmensajeriauem.model;

import org.bson.types.ObjectId;

public class UserSettings {

    private ObjectId id;
    private ObjectId userId;
    private boolean darkMode;
    private String wallpaperPath;
    private String displayName;
    private String status;

    public UserSettings() {}

    public UserSettings(ObjectId id, ObjectId userId, boolean darkMode,
                        String wallpaperPath, String displayName, String status) {
        this.id = id;
        this.userId = userId;
        this.darkMode = darkMode;
        this.wallpaperPath = wallpaperPath;
        this.displayName = displayName;
        this.status = status;
    }

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public ObjectId getUserId() { return userId; }
    public void setUserId(ObjectId userId) { this.userId = userId; }

    public boolean isDarkMode() { return darkMode; }
    public void setDarkMode(boolean darkMode) { this.darkMode = darkMode; }

    public String getWallpaperPath() { return wallpaperPath; }
    public void setWallpaperPath(String wallpaperPath) { this.wallpaperPath = wallpaperPath; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
