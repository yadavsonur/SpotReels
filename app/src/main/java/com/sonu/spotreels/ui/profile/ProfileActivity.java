package com.sonu.spotreels.ui.profile;

import android.os.Bundle;
import android.view.View;

import androidx.media3.common.util.UnstableApi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.sonu.spotreels.R;
import com.sonu.spotreels.adapters.ReelsAdapter;
import com.sonu.spotreels.model.Reel;
import com.sonu.spotreels.player.PlayerManager;

import java.util.ArrayList;
import java.util.List;

@UnstableApi

public class ProfileActivity extends AppCompatActivity {

    private RecyclerView rvMyUploads;
    private ReelsAdapter adapter;
    private final List<Reel> myList = new ArrayList<>();

    private LinearLayoutManager layoutManager;
    private SnapHelper snapHelper;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration profileListener;   // ✅ MEMORY-LEAK FIX
    private String uid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        uid = FirebaseAuth.getInstance().getUid();

        // ✅ UID SAFETY CHECK
        if (uid == null || uid.isEmpty()) {
            finish();
            return;
        }

        rvMyUploads = findViewById(R.id.rvMyUploads);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvMyUploads.setLayoutManager(layoutManager);

        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvMyUploads);

        adapter = new ReelsAdapter(myList, null);
        rvMyUploads.setAdapter(adapter);


        rvMyUploads.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    playCurrent();
                } else {
                    PlayerManager.getInstance(ProfileActivity.this).pause();
                }
            }
        });

        loadMyReels();
    }

    private void loadMyReels() {

        // ✅ USER-FILTERED + MEMORY-SAFE LISTENER
        profileListener = db.collection("reels")
                .whereEqualTo("uploadedBy", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, err) -> {

                    if (err != null) return;

                    myList.clear();

                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            Reel r = d.toObject(Reel.class);
                            if (r != null) myList.add(r);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    rvMyUploads.post(this::playCurrent);
                });
    }

    private int getSnappedPosition() {
        if (snapHelper == null || layoutManager == null)
            return RecyclerView.NO_POSITION;

        View snappedView = snapHelper.findSnapView(layoutManager);
        if (snappedView == null)
            return RecyclerView.NO_POSITION;

        return layoutManager.getPosition(snappedView);
    }

    private void playCurrent() {
        int pos = getSnappedPosition();
        if (pos == RecyclerView.NO_POSITION || pos >= adapter.getItemCount())
            return;

        // 1️⃣ Detach from all visible
        for (int i = 0; i < rvMyUploads.getChildCount(); i++) {
            View child = rvMyUploads.getChildAt(i);
            PlayerView pv = child.findViewById(R.id.playerView);
            if (pv != null) {
                pv.setPlayer(null);
            }
        }

        // 2️⃣ Attach to snapped
        RecyclerView.ViewHolder vh = rvMyUploads.findViewHolderForAdapterPosition(pos);
        if (vh == null) return;

        PlayerView pv = vh.itemView.findViewById(R.id.playerView);
        String url = adapter.getUrlAt(pos);

        if (pv != null && url != null && !url.isEmpty()) {
            PlayerManager.getInstance(this)
                    .playUrlOnPlayerView(url, pv);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        PlayerManager.getInstance(this).pause();   // ✅ pause only
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // ✅ REMOVE FIRESTORE LISTENER
        if (profileListener != null) {
            profileListener.remove();
        }

        // ❌ DO NOT RELEASE PLAYER HERE (shared singleton)
    }
}
