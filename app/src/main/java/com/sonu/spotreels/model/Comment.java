package com.sonu.spotreels.model;

public class Comment {

    private String id;
    private String userId;
    private String username;
    private String text;
    private long timestamp;

    // ✅ REQUIRED EMPTY CONSTRUCTOR FOR FIRESTORE
    public Comment() {
        // Firestore needs this
    }

    // ✅ FULL CONSTRUCTOR
    public Comment(String id, String userId, String username, String text, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.text = text;
        this.timestamp = timestamp;
    }

    // ✅ NULL-SAFE GETTERS (CRASH-PROOF)

    public String getId() {
        return id != null ? id : "";   // ✅ prevents stableId/hash crash
    }

    public String getUserId() {
        return userId != null ? userId : "";
    }

    public String getUsername() {
        return username != null ? username : "User";   // ✅ safe fallback
    }

    public String getText() {
        return text != null ? text : "";   // ✅ prevents adapter crash
    }

    public long getTimestamp() {
        return timestamp > 0 ? timestamp : System.currentTimeMillis();
    }

    // ✅ SAFE SETTERS (OPTIONAL BUT PRODUCTION-GRADE)

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
