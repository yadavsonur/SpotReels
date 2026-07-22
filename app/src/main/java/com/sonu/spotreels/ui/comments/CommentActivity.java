package com.sonu.spotreels.ui.comments;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.sonu.spotreels.R;
import com.sonu.spotreels.adapters.CommentAdapter;
import com.sonu.spotreels.model.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommentActivity extends AppCompatActivity {

    private RecyclerView rvComments;
    private EditText etComment;
    private ImageButton btnSend;

    private CommentAdapter adapter;
    private final List<Comment> commentList = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration commentListener; // ✅ MEMORY LEAK FIX
    private String reelId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        reelId = getIntent().getStringExtra("reelId");

        // ✅ REEL ID SAFETY
        if (reelId == null || reelId.isEmpty()) {
            finish();
            return;
        }

        rvComments = findViewById(R.id.rvComments);
        etComment = findViewById(R.id.etComment);
        btnSend = findViewById(R.id.btnSend);

        rvComments.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CommentAdapter(commentList);
        rvComments.setAdapter(adapter);

        loadComments();

        btnSend.setOnClickListener(v -> sendComment());
    }

    // ✅ REALTIME COMMENTS + MEMORY SAFE
    private void loadComments() {

        commentListener = db.collection("reels")
                .document(reelId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, err) -> {

                    if (err != null || snap == null) return;

                    commentList.clear();

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Comment c = d.toObject(Comment.class);
                        if (c != null) commentList.add(c);
                    }

                    adapter.notifyDataSetChanged();

                    // ✅ AUTO-SCROLL TO LATEST COMMENT
                    rvComments.scrollToPosition(commentList.size() - 1);
                });
    }

    // ✅ SEND COMMENT – FULLY SAFE & PROTECTED
    private void sendComment() {

        String text = etComment.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSend.setEnabled(false);  // ✅ PREVENT SPAM DOUBLE SEND

        String userId = FirebaseAuth.getInstance().getUid();

        String username = FirebaseAuth.getInstance().getCurrentUser().getEmail();


        String commentId = UUID.randomUUID().toString();

        Comment c = new Comment(
                commentId,
                userId,
                username,
                text,
                System.currentTimeMillis()
        );

        db.collection("reels")
                .document(reelId)
                .collection("comments")
                .document(commentId)
                .set(c)
                .addOnSuccessListener(a -> {
                    etComment.setText("");
                    btnSend.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send comment", Toast.LENGTH_SHORT).show();
                    btnSend.setEnabled(true);
                });
    }

    // ✅ REMOVE REALTIME LISTENER (CRITICAL)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commentListener != null) {
            commentListener.remove();
        }
    }
}
