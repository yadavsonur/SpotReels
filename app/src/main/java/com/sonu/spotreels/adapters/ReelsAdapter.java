package com.sonu.spotreels.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;

import com.sonu.spotreels.player.PlayerManager;

import com.bumptech.glide.Glide; //imported to randomly select profile picture


import androidx.annotation.NonNull;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.sonu.spotreels.R;
import com.sonu.spotreels.manager.LikeManager;
import com.sonu.spotreels.model.Reel;
import com.sonu.spotreels.ui.comments.CommentActivity;

import java.util.ArrayList;
import java.util.List;

public class ReelsAdapter extends RecyclerView.Adapter<ReelsAdapter.ReelViewHolder> {

    public interface Callback {
        void onBindPlayer(PlayerView view, int position);
    }

    private final List<Reel> reels;
    private final Callback callback;

    public ReelsAdapter(List<Reel> reels, Callback callback) {
        this.reels = reels;
        this.callback = callback;
        setHasStableIds(true);   // ✅ Correctly used now
    }

    // ✅ STABLE ID IMPLEMENTATION (MANDATORY)
    @Override
    public long getItemId(int position) {
        if (reels == null || position < 0 || position >= reels.size())
            return RecyclerView.NO_ID;

        String id = reels.get(position).getId();
        return id != null ? id.hashCode() : RecyclerView.NO_ID;
    }

    @NonNull
    @Override
    public ReelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reel, parent, false);
        return new ReelViewHolder(view);
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onViewRecycled(@NonNull ReelViewHolder holder) {
        super.onViewRecycled(holder);

        // Pause playback
        PlayerManager.getInstance(holder.itemView.getContext()).pause();

        // VERY IMPORTANT: detach old view
        holder.playerView.setPlayer(null);
    }



    @Override
    public void onBindViewHolder(@NonNull ReelViewHolder holder, int position) {

        Reel item = reels.get(position);
        if (item == null) return;

        String reelId = item.getId();
        String uid = FirebaseAuth.getInstance().getUid();

        holder.tvCaption.setText(item.getCaption() == null ? "" : item.getCaption());

        // ✅ SIMPLE RANDOM PROFILE IMAGE (NO FIRESTORE)
        String uploaderId = item.getUploadedBy(); // UID of reel owner

        String avatarUrl = "https://api.dicebear.com/7.x/personas/png?seed=" + uploaderId;

        Glide.with(holder.ivProfilePic.getContext())
                .load(avatarUrl)
                .placeholder(R.drawable.ic_profile)
                .into(holder.ivProfilePic);


        // ✅ Bind player safely
        //if (callback != null) {
           // callback.onBindPlayer(holder.playerView, position);
        //}

        // ✅ Comment button
        holder.btnComment.setOnClickListener(v -> {
            if (reelId == null || reelId.isEmpty()) return;

            Intent i = new Intent(v.getContext(), CommentActivity.class);
            i.putExtra("reelId", reelId);
            v.getContext().startActivity(i);
        });

        // ✅ Share button (safe)
        holder.btnShare.setOnClickListener(v -> {
            String url = item.getVideoUrl();
            if (url == null || url.isEmpty()) return;

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, url);
            sendIntent.setType("text/plain");
            v.getContext().startActivity(
                    Intent.createChooser(sendIntent, "Share Reel")
            );
        });

        // ✅ LIKE STATE SAFETY
        boolean isLiked = false;
        if (uid != null && item.getLikedBy() != null) {
            isLiked = item.getLikedBy().contains(uid);
        }

        holder.btnLike.setImageResource(
                isLiked ? R.drawable.ic_like_filled : R.drawable.ic_like_outline
        );

        // ✅ TOGGLE LIKE (PRODUCTION SAFE)
        holder.btnLike.setOnClickListener(v -> {

            if (uid == null || reelId == null || reelId.isEmpty()) return;

            LikeManager.toggleLike(reelId, uid, new LikeManager.LikeCallback() {
                @Override
                public void onResult(boolean nowLiked) {

                    // ✅ Prevent ViewHolder reuse bug
                    int adapterPos = holder.getBindingAdapterPosition(); // ✅ Correct

                    if (adapterPos == RecyclerView.NO_POSITION) return;

                    Reel current = reels.get(adapterPos);

                    // ✅ Update local memory safely
                    if (current.getLikedBy() == null) {
                        current.setLikedBy(new ArrayList<>());
                    }

                    if (nowLiked) {
                        if (!current.getLikedBy().contains(uid)) {
                            current.getLikedBy().add(uid);
                        }
                    } else {
                        current.getLikedBy().remove(uid);
                    }

                    // ✅ Update UI icon safely
                    holder.btnLike.setImageResource(
                            nowLiked ? R.drawable.ic_like_filled : R.drawable.ic_like_outline
                    );
                }

                @Override
                public void onError(Exception e) {
                    Log.e("LIKE", "Error updating like", e);
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return reels == null ? 0 : reels.size();
    }

    // ✅ SAFE VIDEO URL GETTER
    public String getUrlAt(int position) {
        if (reels == null || position < 0 || position >= reels.size())
            return null;

        String url = reels.get(position).getVideoUrl();
        return (url == null || url.isEmpty()) ? null : url;
    }

    static class ReelViewHolder extends RecyclerView.ViewHolder {
        ImageButton btnComment, btnShare, btnLike;
        PlayerView playerView;
        TextView tvCaption;
        ImageView ivProfilePic;

        ReelViewHolder(@NonNull View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.playerView);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);
            ivProfilePic = itemView.findViewById(R.id.ivProfilePic);
        }
    }
}
