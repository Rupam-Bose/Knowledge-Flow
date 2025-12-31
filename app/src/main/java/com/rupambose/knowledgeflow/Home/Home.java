package com.rupambose.knowledgeflow.Home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rupambose.knowledgeflow.All_questions;
import com.rupambose.knowledgeflow.Ask_question;
import com.rupambose.knowledgeflow.BlogWriting.BlogPost;
import com.rupambose.knowledgeflow.Home.adapter.RecyclerViewAdapter;
import com.rupambose.knowledgeflow.Home.adapter.blogItem;
import com.rupambose.knowledgeflow.MainProfile;
import com.rupambose.knowledgeflow.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Home extends AppCompatActivity {

    ImageView cover;
    FloatingActionButton floating_add, floating_writing, floating_question, floating_question_answer, floating_bookmarks;
    Animation animation_rotate_open_anim, animation_rotate_close_anim, animation_from_bottom, animation_to_bottom;
    boolean clicked = true;

    private RecyclerViewAdapter adapter;
    private final List<blogItem> posts = new ArrayList<>();
    private static final String DB_URL = "https://knowledge-flow-87853-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private AutoCompleteTextView searchInput;
    private ArrayAdapter<String> suggestionsAdapter;
    private final List<String> titleSuggestions = new ArrayList<>();
    private final List<String> titleKeys = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cover = findViewById(R.id.profileicon);
        floating_add = findViewById(R.id.floating_add);
        floating_writing = findViewById(R.id.floating_writing);
        floating_question = findViewById(R.id.floating_question);
        floating_question_answer = findViewById(R.id.floating_question_answer);
        floating_bookmarks = findViewById(R.id.floating_bookmarks);
        searchInput = findViewById(R.id.searchInput);

        // show FAB menu again
        floating_add.setVisibility(View.VISIBLE);
        floating_writing.setVisibility(View.INVISIBLE);
        floating_question.setVisibility(View.INVISIBLE);
        floating_question_answer.setVisibility(View.INVISIBLE);
        floating_bookmarks.setVisibility(View.INVISIBLE);

        animation_rotate_open_anim = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        animation_rotate_close_anim = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        animation_from_bottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom);
        animation_to_bottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom);


        floating_add.setOnClickListener(v -> {
            SetVisibility(clicked);
            SetAnimation(clicked);
            clicked = !clicked;
        });

        floating_question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, Ask_question.class);
                startActivity(intent);
            }
        });

        floating_question_answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, All_questions.class);
                startActivity(intent);
            }
        });


        floating_writing.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, com.rupambose.knowledgeflow.BlogWriting.WriteBlogActivity.class);
            startActivity(intent);
        });


        floating_bookmarks.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, BookmarksActivity.class);
            startActivity(intent);
        });

        DatabaseReference ref = FirebaseDatabase.getInstance(DB_URL)
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getUid())
                .child("profileImage");

        ref.get().addOnSuccessListener(snapshot -> {
            String url = snapshot.getValue(String.class);
            if (url != null && !url.isEmpty()) {
                Glide.with(this).load(url).into(cover);
            }
        });

        cover.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, MainProfile.class);
            startActivity(intent);
        });

        RecyclerView recyclerView = findViewById(R.id.blogRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter(this);
        recyclerView.setAdapter(adapter);

        suggestionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, titleSuggestions);
        searchInput.setAdapter(suggestionsAdapter);

        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filterByTitle(s == null ? "" : s.toString());
                filterSuggestions(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) { }
        });

        searchInput.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= titleKeys.size()) return;
            String key = titleKeys.get(position);
            openPostByKey(key);
        });

        listenForPosts();
    }

    private void listenForPosts() {
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("posts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        posts.clear();
                        titleSuggestions.clear();
                        titleKeys.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            blogItem item = child.getValue(blogItem.class);
                            if (item != null) {
                                item.setKey(child.getKey());
                                posts.add(item);
                                titleSuggestions.add(item.getContentTitle() == null ? "" : item.getContentTitle());
                                titleKeys.add(child.getKey());
                            }
                        }
                        Collections.sort(posts, Comparator.comparingLong(blogItem::getTimestamp).reversed());
                        adapter.setItems(posts);
                        suggestionsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(Home.this, "Failed to load posts", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterSuggestions(String query) {
        // AutoCompleteTextView handles filtering its adapter; we just ensure the list is current.
        suggestionsAdapter.getFilter().filter(query);
    }

    private void openPostByKey(String key) {
        blogItem item = adapter.getItemByKey(key);
        if (item == null) return;
        Intent intent = new Intent(this, BlogPost.class);
        intent.putExtra("postId", item.getKey());
        intent.putExtra("title", item.getContentTitle());
        intent.putExtra("content", item.getPost());
        intent.putExtra("profileName", item.getProfileName());
        intent.putExtra("profilePic", item.getProfilePic());
        intent.putExtra("date", item.getDate());
        intent.putExtra("likesCount", item.getLikesCount());
        intent.putExtra("commentsCount", item.getCommentsCount());
        intent.putExtra("authorUid", item.getUid());
        startActivity(intent);
    }

    private void SetVisibility(boolean clicked) {
        if (clicked) {
            floating_writing.setVisibility(View.VISIBLE);
            floating_question_answer.setVisibility(View.VISIBLE);
            floating_question.setVisibility(View.VISIBLE);
            floating_bookmarks.setVisibility(View.VISIBLE);
        } else {
            floating_writing.setVisibility(View.INVISIBLE);
            floating_question_answer.setVisibility(View.INVISIBLE);
            floating_question.setVisibility(View.INVISIBLE);
            floating_bookmarks.setVisibility(View.INVISIBLE);
        }
    }

    private void SetAnimation(boolean clicked) {
        if (clicked) {
            floating_writing.startAnimation(animation_from_bottom);
            floating_question.startAnimation(animation_from_bottom);
            floating_question_answer.startAnimation(animation_from_bottom);
            floating_bookmarks.startAnimation(animation_from_bottom);
            floating_add.startAnimation(animation_rotate_open_anim);
        } else {
            floating_writing.startAnimation(animation_to_bottom);
            floating_question_answer.startAnimation(animation_to_bottom);
            floating_question.startAnimation(animation_to_bottom);
            floating_bookmarks.startAnimation(animation_to_bottom);
            floating_add.startAnimation(animation_rotate_close_anim);
        }
    }
}
