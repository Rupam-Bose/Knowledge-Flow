package com.rupambose.knowledgeflow;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rupambose.knowledgeflow.Models.Question;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Ask_question extends AppCompatActivity {

    private static final String DB_URL = "https://knowledge-flow-87853-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private EditText questionInput;
    private Button btnAsk;
    private RecyclerView recyclerView;
    private QuestionsAdapter adapter;
    private final List<Question> questions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ask_question);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        questionInput = findViewById(R.id.questionInput);
        btnAsk = findViewById(R.id.btnAsk);
        recyclerView = findViewById(R.id.questionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuestionsAdapter(this, questions);
        recyclerView.setAdapter(adapter);

        btnAsk.setOnClickListener(v -> submitQuestion());
        listenForQuestions();
    }

    private void submitQuestion() {
        String text = questionInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            questionInput.setError("Please enter a question");
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
            public void onDataChange(DataSnapshot snapshot) {
                String userName = snapshot.child("name").getValue(String.class);
                String profilePic = snapshot.child("profileImage").getValue(String.class);
                userName = userName != null ? userName : "Anonymous";
                profilePic = profilePic != null ? profilePic : "";

                long ts = System.currentTimeMillis();
                String date = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        .format(new Date(ts));

                DatabaseReference qRef = FirebaseDatabase.getInstance(DB_URL)
                        .getReference("questions")
                        .push();

                Question q = new Question();
                q.questionId = qRef.getKey();
                q.questionText = text;
                q.uid = uid;
                q.userName = userName;
                q.profilePic = profilePic;
                q.timestamp = ts;
                q.date = date;
                q.likesCount = 0;
                q.answersCount = 0;

                qRef.setValue(q).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        questionInput.setText("");
                        Toast.makeText(Ask_question.this, "Question posted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Ask_question.this, All_questions.class));
                    } else {
                        Toast.makeText(Ask_question.this, "Failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Ask_question.this, "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForQuestions() {
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("questions")
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        questions.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Question q = child.getValue(Question.class);
                            if (q != null) {
                                q.questionId = child.getKey();
                                questions.add(0, q);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(Ask_question.this, "Load failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}