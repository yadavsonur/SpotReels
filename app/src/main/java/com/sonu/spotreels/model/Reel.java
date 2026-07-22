package com.sonu.spotreels.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Reel {

    private String id;
    private String uploadedBy;
    private String videoUrl;
    private String caption;
    private long timestamp;

    private long likes;
    private List<String> likedBy;

    // ✅ REQUIRED FOR FIRESTORE
    public Reel() {
        // Firestore needs empty constructor
    }

    // ✅ OPTIONAL LOCAL CONSTRUCTOR (NOT USED BY FIRESTORE)
    public Reel(String videoUrl, String caption, String uploadedBy) {
        this.id = UUID.randomUUID().toString();
        this.videoUrl = videoUrl;
        this.caption = caption;
        this.uploadedBy = uploadedBy;
        this.timestamp = System.currentTimeMillis();
        this.likes = 0;
        this.likedBy = new ArrayList<>();
    }

    // ✅ SAFE GETTERS (NULL-PROOF)

    public String getId() {
        return id != null ? id : "";   // ✅ prevents stableId() crash
    }

    public String getUploadedBy() {
        return uploadedBy != null ? uploadedBy : "";
    }

    public String getVideoUrl() {
        return videoUrl != null ? videoUrl : "";
    }

    public String getCaption() {
        return caption != null ? caption : "";
    }

    public long getTimestamp() {
        return timestamp > 0 ? timestamp : System.currentTimeMillis();
    }

    public long getLikes() {
        return likes;
    }

    public List<String> getLikedBy() {
        if (likedBy == null) {
            likedBy = new ArrayList<>();   // ✅ NULL SAFETY CRITICAL FOR LIKES
        }
        return likedBy;
    }

    // ✅ PRODUCTION-SAFE SETTERS

    public void setId(String id) {
        this.id = id;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public void setLikedBy(List<String> likedBy) {
        this.likedBy = (likedBy != null) ? likedBy : new ArrayList<>();
    }
}
