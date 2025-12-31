package com.rupambose.knowledgeflow;

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
import com.rupambose.knowledgeflow.Models.Answer;

import java.util.List;

public class AnswerAdapter extends RecyclerView.Adapter<AnswerAdapter.ViewHolder> {
    private final Context context;
    private final List<Answer> items;

    public AnswerAdapter(Context context, List<Answer> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_answer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Answer a = items.get(position);
        holder.answerText.setText(a.answerText);
        holder.userName.setText(a.userName);
        holder.timeText.setText(a.date != null ? a.date : "");

        Glide.with(holder.itemView)
                .load(a.profilePic)
                .placeholder(R.drawable.profileicon)
                .error(R.drawable.profileicon)
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.profilePic);

        View.OnClickListener openAuthor = v -> {
            if (a.uid == null || a.uid.isEmpty()) return;
            Context c = holder.itemView.getContext();
            Intent intent = new Intent(c, UserProfileActivity.class);
            intent.putExtra("userId", a.uid);
            c.startActivity(intent);
        };
        holder.profilePic.setOnClickListener(openAuthor);
        holder.userName.setOnClickListener(openAuthor);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePic;
        TextView userName;
        TextView timeText;
        TextView answerText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.answerProfilePic);
            userName = itemView.findViewById(R.id.answerUserName);
            timeText = itemView.findViewById(R.id.answerTimeText);
            answerText = itemView.findViewById(R.id.answerText);
        }
    }
}
