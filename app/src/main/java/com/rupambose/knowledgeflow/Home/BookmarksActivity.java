package com.rupambose.knowledgeflow.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.rupambose.knowledgeflow.Home.adapter.RecyclerViewAdapter;
import com.rupambose.knowledgeflow.Home.adapter.blogItem;
import com.rupambose.knowledgeflow.MainProfile;
import com.rupambose.knowledgeflow.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BookmarksActivity extends AppCompatActivity {

    private ImageView cover;
    private RecyclerViewAdapter adapter;
    private final List<blogItem> posts = new ArrayList<>();
    private static final String DB_URL = "https://knowledge-flow-87853-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bookmarks);
        applyInsets();



        cover = findViewById(R.id.profileicon);

        RecyclerView recyclerView = findViewById(R.id.blogRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter(this);
        recyclerView.setAdapter(adapter);

        cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BookmarksActivity.this, MainProfile.class);
                startActivity(intent);
            }
        });

        loadProfile();
        listenForBookmarks();
    }



    private void applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadProfile() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance(DB_URL)
                .getReference("Users")
                .child(userId)
                .child("profileImage");
        ref.get().addOnSuccessListener(snapshot -> {
            String url = snapshot.getValue(String.class);
            if (url != null && !url.isEmpty()) {
                Glide.with(this).load(url).into(cover);
            }
        });
    }

    private void listenForBookmarks() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        DatabaseReference db = FirebaseDatabase.getInstance(DB_URL).getReference();
        db.child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                posts.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    if (child.child("bookmarks").hasChild(uid)) {
                        blogItem item = child.getValue(blogItem.class);
                        if (item != null) {
                            item.setKey(child.getKey());
                            item.setBookmarked(true);
                            posts.add(item);
                        }
                    }
                }
                Collections.sort(posts, Comparator.comparingLong(blogItem::getTimestamp).reversed());
                adapter.setItems(posts);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(BookmarksActivity.this, "Failed to load bookmarks", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

