package com.rupambose.knowledgeflow.BlogWriting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.rupambose.knowledgeflow.Home.Home;
import com.rupambose.knowledgeflow.R;

public class WriteBlogActivity extends AppCompatActivity {

    EditText etTitle, etContent;
    Button btnDraft, btnPublish;

    SharedPreferences draftPrefs;

    private static final String PREF_NAME = "blog_draft";
    private static final String KEY_TITLE = "draft_title";
    private static final String KEY_CONTENT = "draft_content";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writeblogactivity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnDraft = findViewById(R.id.btnDraft);
        btnPublish = findViewById(R.id.btnPublish);

        draftPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        loadDraft();

        btnDraft.setOnClickListener(v -> saveDraft());

        // PUBLISH BUTTON
        btnPublish.setOnClickListener(v -> publishBlog());
    }

    // Save draft locally
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
        Intent intent = new Intent(WriteBlogActivity.this, Home.class);
        startActivity(intent);
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

        // TODO: Upload blog to server / Firebase

        draftPrefs.edit().clear().apply();

        Toast.makeText(this, "Blog published successfully", Toast.LENGTH_SHORT).show();

        etTitle.setText("");
        etContent.setText("");
    }
}
