package com.rupambose.knowledgeflow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.rupambose.knowledgeflow.Models.Answer;
import com.rupambose.knowledgeflow.Models.Question;
import com.rupambose.knowledgeflow.question_detail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.ViewHolder> {

    private static final String DB_URL = "https://knowledge-flow-87853-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private final Context context;
    private final List<Question> items;

    public QuestionsAdapter(Context context, List<Question> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_questions, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Question q = items.get(position);
        holder.questionText.setText(q.questionText);
        holder.userName.setText(q.userName);
        holder.timeText.setText(q.date != null ? q.date : "");
        holder.likeCount.setText(String.valueOf(q.likesCount));

        Glide.with(holder.itemView)
                .load(q.profilePic)
                .placeholder(R.drawable.profileicon)
                .error(R.drawable.profileicon)
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.profileChar);

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null && q.questionId != null) {
            DatabaseReference likeRef = FirebaseDatabase.getInstance(DB_URL)
                    .getReference("questions")
                    .child(q.questionId)
                    .child("likes")
                    .child(uid);

            likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean liked = snapshot.exists();
                    q.likes = q.likes != null ? q.likes : null; // keep map reference if loaded
                    holder.likeButton.setImageResource(liked ? R.drawable.heart_red : R.drawable.heart_black);
                    holder.likeButton.setTag(liked);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }

        holder.likeButton.setOnClickListener(v -> {
            String currentUid = FirebaseAuth.getInstance().getUid();
            if (currentUid == null || q.questionId == null) return;
            boolean currentlyLiked = holder.likeButton.getTag() instanceof Boolean && (Boolean) holder.likeButton.getTag();
            int newCount = Math.max(q.likesCount + (currentlyLiked ? -1 : 1), 0);

            DatabaseReference qRef = FirebaseDatabase.getInstance(DB_URL)
                    .getReference("questions")
                    .child(q.questionId);

            if (currentlyLiked) {
                qRef.child("likes").child(currentUid).removeValue();
            } else {
                qRef.child("likes").child(currentUid).setValue(true);
            }
            qRef.child("likesCount").setValue(newCount);

            q.likesCount = newCount;
            holder.likeCount.setText(String.valueOf(newCount));
            holder.likeButton.setImageResource(currentlyLiked ? R.drawable.heart_black : R.drawable.heart_red);
            holder.likeButton.setTag(!currentlyLiked);
        });

        holder.itemView.setOnClickListener(v -> {
            if (q.questionId == null) return;
            question_detail.start(context, q.questionId);
        });

        holder.btnAnswer.setOnClickListener(v -> {
            String answerText = holder.answerInput.getText().toString().trim();
            if (TextUtils.isEmpty(answerText)) {
                Toast.makeText(context, "Answer cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            submitAnswer(q, answerText);
        });
    }

    private void showAnswerDialog(Question q) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_answer, null);
        EditText answerInput = dialogView.findViewById(R.id.answerInput);

        new AlertDialog.Builder(context)
                .setTitle("Your Answer")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String answerText = answerInput.getText().toString().trim();
                    if (TextUtils.isEmpty(answerText)) {
                        Toast.makeText(context, "Answer cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    submitAnswer(q, answerText);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitAnswer(Question q, String answerText) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || q.questionId == null) {
            Toast.makeText(context, "Please sign in first", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("Users")
                .child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.child("name").getValue(String.class);
                String profilePic = snapshot.child("profileImage").getValue(String.class);
                userName = userName != null ? userName : "Anonymous";
                profilePic = profilePic != null ? profilePic : "";

                long ts = System.currentTimeMillis();
                String date = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        .format(new Date(ts));

                DatabaseReference answersRef = FirebaseDatabase.getInstance(DB_URL)
                        .getReference("questions")
                        .child(q.questionId)
                        .child("answers");

                String answerId = answersRef.push().getKey();
                if (answerId == null) return;

                Answer ans = new Answer();
                ans.answerId = answerId;
                ans.answerText = answerText;
                ans.uid = uid;
                ans.userName = userName;
                ans.profilePic = profilePic;
                ans.timestamp = ts;
                ans.date = date;

                answersRef.child(answerId).setValue(ans);
                FirebaseDatabase.getInstance(DB_URL)
                        .getReference("questions")
                        .child(q.questionId)
                        .child("answersCount")
                        .setValue(q.answersCount + 1);

                Toast.makeText(context, "Answer posted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileChar;
        TextView userName;
        TextView timeText;
        TextView questionText;
        ImageView likeButton;
        TextView likeCount;
        Button btnAnswer;
        EditText answerInput;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileChar = itemView.findViewById(R.id.profilepic);
            userName = itemView.findViewById(R.id.userName);
            timeText = itemView.findViewById(R.id.timeText);
            questionText = itemView.findViewById(R.id.questionText);
            likeButton = itemView.findViewById(R.id.likeButton);
            likeCount = itemView.findViewById(R.id.likeCount);
            btnAnswer = itemView.findViewById(R.id.btnAnswer);
            answerInput = itemView.findViewById(R.id.answerInput);
        }
    }
}
