package com.rupambose.knowledgeflow.BlogWriting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.rupambose.knowledgeflow.Models.Comment;
import com.rupambose.knowledgeflow.R;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private final Context context;
    private final List<Comment> items;

    public CommentAdapter(Context context, List<Comment> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment c = items.get(position);
        holder.userName.setText(c.userName);
        holder.commentText.setText(c.text);
        holder.date.setText(c.date);
        holder.likeCount.setText(String.valueOf(c.likesCount));

        Glide.with(holder.itemView)
                .load(c.profilePic)
                .placeholder(R.drawable.profileicon)
                .error(R.drawable.profileicon)
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.profilePic);

        holder.likeIcon.setImageResource(c.liked ? R.drawable.heart_red : R.drawable.heart_black);

        holder.likeIcon.setOnClickListener(v -> {
            if (listener != null) listener.onLikeClicked(c);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface CommentActionListener {
        void onLikeClicked(Comment comment);
    }

    private CommentActionListener listener;

    public void setListener(CommentActionListener listener) {
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView profilePic;
        final TextView userName;
        final TextView date;
        final TextView commentText;
        final ImageView likeIcon;
        final TextView likeCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.commentProfilePic);
            userName = itemView.findViewById(R.id.commentUserName);
            date = itemView.findViewById(R.id.commentDate);
            commentText = itemView.findViewById(R.id.commentText);
            likeIcon = itemView.findViewById(R.id.commentLike);
            likeCount = itemView.findViewById(R.id.commentLikeCount);
        }
    }
}

