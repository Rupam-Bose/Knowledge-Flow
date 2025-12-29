package com.rupambose.knowledgeflow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class question_detail extends AppCompatActivity {

    public static final String EXTRA_QUESTION_ID = "question_id";
    private static final String DB_URL = "https://knowledge-flow-87853-default-rtdb.asia-southeast1.firebasedatabase.app/";

    public static void start(Context context, String questionId) {
        Intent intent = new Intent(context, question_detail.class);
        intent.putExtra(EXTRA_QUESTION_ID, questionId);
        context.startActivity(intent);
    }

    private ImageView profileChar, likeButton;
    private TextView userName, timeText, questionText, likeCount;
    private EditText answerInput;
    private Button btnSubmitAnswer;
    private RecyclerView answersRecyclerView;

    private final List<Answer> answers = new ArrayList<>();
    private AnswerAdapter answerAdapter;
    private Question currentQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        String questionId = resolveQuestionId();
        if (TextUtils.isEmpty(questionId)) {
            fetchLatestQuestionAndLoad();
            return;
        }

        bindViews();
        setupAnswersList();
        loadQuestion(questionId);
        listenAnswers(questionId);

        btnSubmitAnswer.setOnClickListener(v -> submitAnswer(questionId));
        likeButton.setOnClickListener(v -> toggleLike(questionId));
    }

    private void fetchLatestQuestionAndLoad() {
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("questions")
                .orderByChild("timestamp")
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        DataSnapshot last = null;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            last = child;
                        }
                        if (last == null) {
                            Toast.makeText(question_detail.this, "No question found", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        String latestId = last.getKey();
                        if (TextUtils.isEmpty(latestId)) {
                            Toast.makeText(question_detail.this, "No question id", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        bindViews();
                        setupAnswersList();
                        loadQuestion(latestId);
                        listenAnswers(latestId);
                        btnSubmitAnswer.setOnClickListener(v -> submitAnswer(latestId));
                        likeButton.setOnClickListener(v -> toggleLike(latestId));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(question_detail.this, "No question id", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private String resolveQuestionId() {
        Intent i = getIntent();
        if (i == null) return null;
        String id = i.getStringExtra(EXTRA_QUESTION_ID);
        if (TextUtils.isEmpty(id)) {
            id = i.getStringExtra("questionId");
        }
        return id;
    }

    private void bindViews() {
        profileChar = findViewById(R.id.profileChar);
        likeButton = findViewById(R.id.likeButton);
        userName = findViewById(R.id.userName);
        timeText = findViewById(R.id.timeText);
        questionText = findViewById(R.id.questionText);
        likeCount = findViewById(R.id.likeCount);
        answerInput = findViewById(R.id.answerInput);
        btnSubmitAnswer = findViewById(R.id.btnSubmitAnswer);
        answersRecyclerView = findViewById(R.id.answersRecyclerView);
    }

    private void setupAnswersList() {
        answersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        answerAdapter = new AnswerAdapter(this, answers);
        answersRecyclerView.setAdapter(answerAdapter);
    }

    private void loadQuestion(String questionId) {
        DatabaseReference ref = FirebaseDatabase.getInstance(DB_URL)
                .getReference("questions")
                .child(questionId);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentQuestion = snapshot.getValue(Question.class);
                if (currentQuestion == null) return;

                userName.setText(currentQuestion.userName);
                timeText.setText(currentQuestion.date != null ? currentQuestion.date : "");
                questionText.setText(currentQuestion.questionText);
                likeCount.setText(String.valueOf(currentQuestion.likesCount));

                Glide.with(question_detail.this)
                        .load(currentQuestion.profilePic)
                        .placeholder(R.drawable.profileicon)
                        .error(R.drawable.profileicon)
                        .circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(profileChar);

                updateLikeIcon(questionId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(question_detail.this, "Failed to load", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLikeIcon(String questionId) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseDatabase.getInstance(DB_URL)
                .getReference("questions")
                .child(questionId)
                .child("likes")
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean liked = snapshot.exists();
                        likeButton.setImageResource(liked ? R.drawable.heart_red : R.drawable.heart_black);
                        likeButton.setTag(liked);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void toggleLike(String questionId) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || currentQuestion == null) return;

        boolean liked = likeButton.getTag() instanceof Boolean && (Boolean) likeButton.getTag();
        int newCount = Math.max(currentQuestion.likesCount + (liked ? -1 : 1), 0);

        DatabaseReference qRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("questions")
                .child(questionId);

        if (liked) {
            qRef.child("likes").child(uid).removeValue();
        } else {
            qRef.child("likes").child(uid).setValue(true);
        }
        qRef.child("likesCount").setValue(newCount);

        currentQuestion.likesCount = newCount;
        likeCount.setText(String.valueOf(newCount));
        likeButton.setImageResource(liked ? R.drawable.heart_black : R.drawable.heart_red);
        likeButton.setTag(!liked);
    }

    private void listenAnswers(String questionId) {
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("questions")
                .child(questionId)
                .child("answers")
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        answers.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Answer a = child.getValue(Answer.class);
                            if (a != null) answers.add(a);
                        }
                        answerAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void submitAnswer(String questionId) {
        String text = answerInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            answerInput.setError("Enter answer");
            return;
        }

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("Users")
                .child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String pic = snapshot.child("profileImage").getValue(String.class);
                name = name != null ? name : "Anonymous";
                pic = pic != null ? pic : "";

                long ts = System.currentTimeMillis();
                String date = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        .format(new Date(ts));

                DatabaseReference answersRef = FirebaseDatabase.getInstance(DB_URL)
                        .getReference("questions")
                        .child(questionId)
                        .child("answers");

                String answerId = answersRef.push().getKey();
                if (answerId == null) return;

                Answer a = new Answer();
                a.answerId = answerId;
                a.answerText = text;
                a.uid = uid;
                a.userName = name;
                a.profilePic = pic;
                a.timestamp = ts;
                a.date = date;

                answersRef.child(answerId).setValue(a);
                FirebaseDatabase.getInstance(DB_URL)
                        .getReference("questions")
                        .child(questionId)
                        .child("answersCount")
                        .setValue(currentQuestion != null ? currentQuestion.answersCount + 1 : 1);

                answerInput.setText("");
                Toast.makeText(question_detail.this, "Answer posted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(question_detail.this, "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
