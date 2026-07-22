package com.sonu.spotreels.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sonu.spotreels.R;
import com.sonu.spotreels.model.Comment;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentVH> {

    private final List<Comment> list;

    public CommentAdapter(List<Comment> list) {
        this.list = list;
        setHasStableIds(true); // ✅ Prevents flickering on realtime updates
    }

    // ✅ STABLE ID SUPPORT (IMPORTANT)
    @Override
    public long getItemId(int position) {
        if (list == null || position < 0 || position >= list.size()) {
            return RecyclerView.NO_ID;
        }
        return list.get(position).getId().hashCode();
    }

    @NonNull
    @Override
    public CommentVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentVH h, int position) {

        // ✅ NULL-SAFE BINDING
        Comment c = list.get(position);
        if (c == null) return;

        h.tvUser.setText(c.getUsername());
        h.tvText.setText(c.getText());
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class CommentVH extends RecyclerView.ViewHolder {
        TextView tvUser, tvText;

        CommentVH(@NonNull View item) {
            super(item);
            tvUser = item.findViewById(R.id.tvUser);
            tvText = item.findViewById(R.id.tvText);
        }
    }
}
