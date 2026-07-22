package com.sonu.spotreels.manager;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LikeManager {

    public interface LikeCallback {
        void onResult(boolean nowLiked);
        void onError(Exception e);
    }

    public static void toggleLike(String reelId, String userId, LikeCallback callback) {

        // ✅ NULL SAFETY FIRST
        if (reelId == null || reelId.isEmpty()) {
            if (callback != null)
                callback.onError(new Exception("Invalid reel ID"));
            return;
        }

        if (userId == null || userId.isEmpty()) {
            if (callback != null)
                callback.onError(new Exception("User not authenticated"));
            return;
        }

        // ✅ GET FIRESTORE SAFELY (NO STATIC CONTEXT LEAK)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("reels").document(reelId);

        db.runTransaction(transaction -> {

                    DocumentSnapshot snap = transaction.get(ref);

                    // ✅ DOCUMENT EXISTENCE CHECK
                    if (!snap.exists()) {
                        throw new RuntimeException("Reel no longer exists");
                    }

                    // ✅ SAFE LIKE COUNT EXTRACTION
                    long likes = 0;
                    Object likeObj = snap.get("likes");
                    if (likeObj instanceof Long) {
                        likes = (Long) likeObj;
                    }

                    // ✅ SAFE LIST EXTRACTION (NO CLASSCAST CRASH)
                    List<String> likedBy = new ArrayList<>();
                    Object raw = snap.get("likedBy");

                    if (raw instanceof List<?>) {
                        for (Object o : (List<?>) raw) {
                            if (o instanceof String) {
                                likedBy.add((String) o);
                            }
                        }
                    }

                    boolean alreadyLiked = likedBy.contains(userId);

                    if (alreadyLiked) {
                        likedBy.remove(userId);
                        likes = Math.max(0, likes - 1);   // ✅ NEVER NEGATIVE
                    } else {
                        likedBy.add(userId);
                        likes += 1;
                    }

                    HashMap<String, Object> data = new HashMap<>();
                    data.put("likes", likes);
                    data.put("likedBy", likedBy);

                    transaction.update(ref, data);

                    return !alreadyLiked; // ✅ TRUE if now liked
                })
                .addOnSuccessListener(result -> {
                    if (callback != null) callback.onResult(result);
                })
                .addOnFailureListener(e -> {
                    Log.e("LIKE_ERROR", "Transaction failed", e);
                    if (callback != null) callback.onError(e);
                });
    }
}
