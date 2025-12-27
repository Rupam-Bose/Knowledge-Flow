package com.rupambose.knowledgeflow.Home.adapter;

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
import com.rupambose.knowledgeflow.R;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private final Context context;
    private final List<blogItem> items = new ArrayList<>();

    public RecyclerViewAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<blogItem> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        blogItem item = items.get(position);

        holder.title.setText(item.getContentTitle());
        holder.profileName.setText(item.getProfileName());
        holder.date.setText(item.getDate());
        holder.content.setText(item.getPost());
        holder.likesCount.setText(String.valueOf(item.getLikesCount()));
        holder.commentsCount.setText(String.valueOf(item.getCommentsCount()));

        Glide.with(holder.itemView)
                .load(item.getProfilePic())
                .placeholder(R.drawable.profileicon)
                .error(R.drawable.profileicon)
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.profilePic);

        // Sync like icon state
        holder.likes.setSelected(item.isLiked());
        holder.likes.setImageResource(item.isLiked() ? R.drawable.heart_red : R.drawable.heart_black);

        holder.likes.setOnClickListener(v -> {
            boolean newLiked = !item.isLiked();
            item.setLiked(newLiked);
            int newCount = item.getLikesCount() + (newLiked ? 1 : -1);
            item.setLikesCount(Math.max(newCount, 0));
            holder.likesCount.setText(String.valueOf(item.getLikesCount()));
            holder.likes.setImageResource(newLiked ? R.drawable.heart_red : R.drawable.heart_black);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView profilePic;
        final TextView title;
        final TextView profileName;
        final TextView date;
        final TextView content;
        final TextView likesCount;
        final TextView commentsCount;
        final ImageView likes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profileicon);
            title = itemView.findViewById(R.id.Blogtitle);
            profileName = itemView.findViewById(R.id.profile_name);
            date = itemView.findViewById(R.id.Date);
            content = itemView.findViewById(R.id.Contents);
            likesCount = itemView.findViewById(R.id.likesCount);
            commentsCount = itemView.findViewById(R.id.CommentsCount);
            likes = itemView.findViewById(R.id.likes);
        }
    }
}
