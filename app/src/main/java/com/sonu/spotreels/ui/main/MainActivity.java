package com.sonu.spotreels.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import androidx.media3.common.util.UnstableApi;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.sonu.spotreels.R;
import com.sonu.spotreels.adapters.ReelsAdapter;
import com.sonu.spotreels.model.Reel;
import com.sonu.spotreels.player.PlayerManager;
import com.sonu.spotreels.ui.profile.ProfileActivity;
import com.sonu.spotreels.ui.upload.SupabaseUploadActivity;

import java.util.ArrayList;
import java.util.List;

@UnstableApi
public class MainActivity extends AppCompatActivity {

    private RecyclerView rvFeed;
    private ReelsAdapter adapter;
    private final List<Reel> reelList = new ArrayList<>();

    private LinearLayoutManager layoutManager;
    private SnapHelper snapHelper;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration reelsListener;   // ✅ MEMORY-LEAK FIX

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bottom Navigation
        ImageButton navHome = findViewById(R.id.navHome);
        ImageButton navUpload = findViewById(R.id.navUpload);
        ImageButton navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            // Already on Home
        });

        navUpload.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SupabaseUploadActivity.class))
        );

        navProfile.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class))
        );

        setupRecyclerView();
        loadReelsFromFirestore();
    }

    private void setupRecyclerView() {
        rvFeed = findViewById(R.id.rvFeed);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvFeed.setLayoutManager(layoutManager);

        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvFeed);

        adapter = new ReelsAdapter(reelList, null); // callback not used anymore
        rvFeed.setAdapter(adapter);

        rvFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    playCurrentSnappedItem();
                } else {
                    PlayerManager.getInstance(MainActivity.this).pause();
                }
            }
        });

        rvFeed.post(this::playCurrentSnappedItem);
    }

    private int getSnappedPosition() {
        if (snapHelper == null || layoutManager == null)
            return RecyclerView.NO_POSITION;

        View snappedView = snapHelper.findSnapView(layoutManager);
        if (snappedView == null)
            return RecyclerView.NO_POSITION;

        return layoutManager.getPosition(snappedView);
    }

    private void playCurrentSnappedItem() {
        int pos = getSnappedPosition();
        if (pos == RecyclerView.NO_POSITION || pos >= adapter.getItemCount())
            return;

        // 1️⃣ Detach player from all visible items
        for (int i = 0; i < rvFeed.getChildCount(); i++) {
            View child = rvFeed.getChildAt(i);
            PlayerView pv = child.findViewById(R.id.playerView);
            if (pv != null) {
                pv.setPlayer(null);
            }
        }

        // 2️⃣ Attach player only to snapped item
        RecyclerView.ViewHolder vh = rvFeed.findViewHolderForAdapterPosition(pos);
        if (vh == null) return;

        PlayerView playerView = vh.itemView.findViewById(R.id.playerView);
        String url = adapter.getUrlAt(pos);

        if (playerView != null && url != null && !url.isEmpty()) {
            PlayerManager.getInstance(this)
                    .playUrlOnPlayerView(url, playerView);
        }
    }


    private void loadReelsFromFirestore() {

        // ✅ MEMORY-SAFE REALTIME LISTENER
        reelsListener = db.collection("reels")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, err) -> {

                    if (err != null) {
                        Log.e("FIRESTORE", "❌ Reel load error", err);
                        return;
                    }

                    reelList.clear();

                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            Reel r = d.toObject(Reel.class);
                            if (r != null) {
                                reelList.add(r);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                    rvFeed.post(this::playCurrentSnappedItem);
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        PlayerManager.getInstance(this).pause();  // ✅ pause only
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // ✅ REMOVE FIRESTORE LISTENER (NO MEMORY LEAK)
        if (reelsListener != null) {
            reelsListener.remove();
        }

        // ❌ DO NOT RELEASE PLAYER HERE (shared across screens)
    }
}
