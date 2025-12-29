package com.rupambose.knowledgeflow.Home.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rupambose.knowledgeflow.BlogWriting.BlogPost;
import com.rupambose.knowledgeflow.MainProfile;
import com.rupambose.knowledgeflow.R;
import com.rupambose.knowledgeflow.UserProfileActivity;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private final Context context;
    private final List<blogItem> items = new ArrayList<>();
    private static final String DB_URL = "https://knowledge-flow-87853-default-rtdb.asia-southeast1.firebasedatabase.app/";

    public RecyclerViewAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<blogItem> data) {
        items.clear();
        if (data != null) items.addAll(data);
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
        String uid = FirebaseAuth.getInstance().getUid();

        holder.title.setText(item.getContentTitle());
        holder.profileName.setText(item.getProfileName());
        holder.date.setText(item.getDate());
        holder.content.setText(item.getPost());
        holder.likesCount.setText(String.valueOf(item.getLikesCount()));
        holder.commentsCount.setText(String.valueOf(item.getCommentsCount()));

        holder.profilePic.setImageResource(R.drawable.profileicon);
        Glide.with(holder.itemView)
                .load(item.getProfilePic())
                .placeholder(R.drawable.profileicon)
                .error(R.drawable.profileicon)
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.profilePic);

        if (uid != null && item.getKey() != null) {
            DatabaseReference postRef = FirebaseDatabase.getInstance(DB_URL)
                    .getReference("posts")
                    .child(item.getKey());

            postRef.child("likes").child(uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean liked = snapshot.exists();
                            item.setLiked(liked);
                            holder.likes.setImageResource(liked ? R.drawable.heart_red : R.drawable.heart_black);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) { }
                    });

            postRef.child("bookmarks").child(uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean bookmarked = snapshot.exists();
                            item.setBookmarked(bookmarked);
                            holder.bookmark.setImageResource(bookmarked ? R.drawable.save_filled : R.drawable.save);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) { }
                    });
        } else {
            holder.likes.setImageResource(item.isLiked() ? R.drawable.heart_red : R.drawable.heart_black);
            holder.bookmark.setImageResource(item.isBookmarked() ? R.drawable.save_filled : R.drawable.save);
        }

        View.OnClickListener openDetail = v -> {
            Intent intent = new Intent(context, BlogPost.class);
            intent.putExtra("postId", item.getKey());
            intent.putExtra("title", item.getContentTitle());
            intent.putExtra("content", item.getPost());
            intent.putExtra("profileName", item.getProfileName());
            intent.putExtra("profilePic", item.getProfilePic());
            intent.putExtra("date", item.getDate());
            intent.putExtra("likesCount", item.getLikesCount());
            intent.putExtra("commentsCount", item.getCommentsCount());
            intent.putExtra("authorUid", item.getUid());
            context.startActivity(intent);
        };

        View.OnClickListener openAuthorProfile = v -> {
            if (item.getUid() == null || item.getUid().isEmpty()) return;
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("userId", item.getUid());
            context.startActivity(intent);
        };

        holder.readMore.setOnClickListener(openDetail);
        holder.title.setOnClickListener(openDetail);
        holder.profilePic.setOnClickListener(openAuthorProfile);
        holder.profileName.setOnClickListener(openAuthorProfile);

        holder.likes.setOnClickListener(v -> {
            if (uid == null || item.getKey() == null) return;

            boolean currentlyLiked = item.isLiked();
            int newCount = Math.max(item.getLikesCount() + (currentlyLiked ? -1 : 1), 0);

            DatabaseReference postRef = FirebaseDatabase.getInstance(DB_URL)
                    .getReference("posts")
                    .child(item.getKey());

            if (currentlyLiked) {
                postRef.child("likes").child(uid).removeValue();
            } else {
                postRef.child("likes").child(uid).setValue(true);
            }
            postRef.child("likesCount").setValue(newCount);

            item.setLiked(!currentlyLiked);
            item.setLikesCount(newCount);
            holder.likesCount.setText(String.valueOf(newCount));
            holder.likes.setImageResource(item.isLiked() ? R.drawable.heart_red : R.drawable.heart_black);
        });

        holder.bookmark.setOnClickListener(v -> {
            if (uid == null || item.getKey() == null) return;
            boolean currentlyBookmarked = item.isBookmarked();
            DatabaseReference postRef = FirebaseDatabase.getInstance(DB_URL)
                    .getReference("posts")
                    .child(item.getKey())
                    .child("bookmarks")
                    .child(uid);
            if (currentlyBookmarked) {
                postRef.removeValue();
            } else {
                postRef.setValue(true);
            }
            item.setBookmarked(!currentlyBookmarked);
            holder.bookmark.setImageResource(item.isBookmarked() ? R.drawable.save_filled : R.drawable.save);
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
        final ImageView bookmark;
        final View readMore;

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
            bookmark = itemView.findViewById(R.id.Save);
            readMore = itemView.findViewById(R.id.ReadMore);
        }
    }
}
