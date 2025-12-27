package com.rupambose.knowledgeflow.BlogWriting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rupambose.knowledgeflow.Home.Home;
import com.rupambose.knowledgeflow.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WriteBlogActivity extends AppCompatActivity {

    EditText etTitle, etContent;
    Button btnDraft, btnPublish;

    SharedPreferences draftPrefs;

    private static final String PREF_NAME = "blog_draft";
    private static final String KEY_TITLE = "draft_title";
    private static final String KEY_CONTENT = "draft_content";
    private static final String DB_URL = "https://knowledge-flow-87853-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writeblogactivity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etTitle = findViewById(R.id.blogTitle);
        etContent = findViewById(R.id.etContent);
        btnDraft = findViewById(R.id.btnDraft);
        btnPublish = findViewById(R.id.btnPublish);

        draftPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        loadDraft();

        btnDraft.setOnClickListener(v -> saveDraft());
        btnPublish.setOnClickListener(v -> publishBlog());
    }

    private void saveDraft() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Nothing to save", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = draftPrefs.edit();
        editor.putString(KEY_TITLE, title);
        editor.putString(KEY_CONTENT, content);
        editor.apply();

        Toast.makeText(this, "Draft saved successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(WriteBlogActivity.this, Home.class));
        finish();
    }

    private void loadDraft() {
        String savedTitle = draftPrefs.getString(KEY_TITLE, "");
        String savedContent = draftPrefs.getString(KEY_CONTENT, "");

        if (!savedTitle.isEmpty() || !savedContent.isEmpty()) {
            etTitle.setText(savedTitle);
            etContent.setText(savedContent);
            Toast.makeText(this, "Draft restored", Toast.LENGTH_SHORT).show();
        }
    }

    private void publishBlog() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title required");
            return;
        }
        if (content.isEmpty()) {
            etContent.setError("Content required");
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

        userRef.get().addOnSuccessListener(snapshot -> {
            String profileName = snapshot.child("name").getValue(String.class);
            String profilePic = snapshot.child("profileImage").getValue(String.class);

            profileName = profileName != null ? profileName : "Anonymous";
            profilePic = profilePic != null ? profilePic : "";

            long timestamp = System.currentTimeMillis();
            String date = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(new Date(timestamp));

            Map<String, Object> postMap = new HashMap<>();
            postMap.put("contentTitle", title);
            postMap.put("post", content);
            postMap.put("profileName", profileName);
            postMap.put("profilePic", profilePic);
            postMap.put("date", date);
            postMap.put("timestamp", timestamp);
            postMap.put("likesCount", 0);
            postMap.put("commentsCount", 0);
            postMap.put("liked", false);
            postMap.put("uid", uid);

            FirebaseDatabase.getInstance(DB_URL)
                    .getReference("posts")
                    .push()
                    .setValue(postMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            draftPrefs.edit().clear().apply();
                            Toast.makeText(this, "Blog published successfully", Toast.LENGTH_SHORT).show();
                            etTitle.setText("");
                            etContent.setText("");
                            Intent intent = new Intent(WriteBlogActivity.this, Home.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            String msg = task.getException() != null ? task.getException().getMessage() : "Publish failed";
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();                        }
                    });
        }).addOnFailureListener(e ->
                        Toast.makeText(this, "Publish failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());    }
}
