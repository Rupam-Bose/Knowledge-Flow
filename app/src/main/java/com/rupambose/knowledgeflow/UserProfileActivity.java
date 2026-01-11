package com.rupambose.knowledgeflow;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rupambose.knowledgeflow.Home.adapter.RecyclerViewAdapter;
import com.rupambose.knowledgeflow.Home.adapter.blogItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView name;
    private TextView email;
    private TextView followersCount;
    private Button followButton;
    private String userId;
    private boolean isFollowing;

    private RecyclerView userPostsRecycler;
    private View postsProgress;
    private TextView emptyPosts;
    private RecyclerViewAdapter postsAdapter;
    private final List<blogItem> userPosts = new ArrayList<>();

    private static final String DB_URL = "https://knowledge-flow-87853-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private boolean isSelf;
    private Query postsQuery;
    private ValueEventListener postsListener;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        profileImage = findViewById(R.id.profile_image);
        name = findViewById(R.id.username);
        email = findViewById(R.id.useremail);
        followersCount = findViewById(R.id.followersCount);
        followButton = findViewById(R.id.followButton);
        userPostsRecycler = findViewById(R.id.userPostsRecycler);
        postsProgress = findViewById(R.id.postsProgress);
        emptyPosts = findViewById(R.id.emptyPosts);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String currentUid = FirebaseAuth.getInstance().getUid();
        isSelf = currentUid != null && currentUid.equals(userId);

        loadUser();
        watchFollowers();
        watchFollowState();
        setupPostsList();
        updatePostsAccess();

        followButton.setOnClickListener(v -> toggleFollow());
    }

    private void loadUser() {
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("Users")
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(UserProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        String uname = snapshot.child("name").getValue(String.class);
                        String mail = snapshot.child("email").getValue(String.class);
                        String pic = snapshot.child("profileImage").getValue(String.class);

                        if (uname != null) name.setText(uname);
                        if (mail != null) email.setText(mail);

                        Glide.with(UserProfileActivity.this)
                                .load(pic)
                                .placeholder(R.drawable.profileicon)
                                .error(R.drawable.profileicon)
                                .circleCrop()
                                .into(profileImage);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(UserProfileActivity.this, "Failed to load user", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void watchFollowers() {
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("followers")
                .child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        long count = snapshot.getChildrenCount();
                        followersCount.setText(count + " followers");
                    }

                    @Override
                    public void onCancelled(DatabaseError error) { }
                });
    }

    private void watchFollowState() {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) {
            updatePostsAccess();
            return;
        }
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("followers")
                .child(userId)
                .child(currentUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        isFollowing = snapshot.exists();
                        followButton.setText(isFollowing ? "Unfollow" : "Follow");
                        updatePostsAccess();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) { }
                });
    }

    private void toggleFollow() {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) {
            Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentUid.equals(userId)) {
            Toast.makeText(this, "You cannot follow yourself", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase.getInstance(DB_URL)
                .getReference("followers")
                .child(userId)
                .child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            snapshot.getRef().removeValue();
                            followButton.setText("Follow");
                        } else {
                            snapshot.getRef().setValue(true);
                            followButton.setText("Unfollow");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) { }
                });
    }

    private void setupPostsList() {
        userPostsRecycler.setLayoutManager(new LinearLayoutManager(this));
        postsAdapter = new RecyclerViewAdapter(this);
        userPostsRecycler.setAdapter(postsAdapter);
    }

    private void updatePostsAccess() {
        boolean canView = isSelf || isFollowing;
        if (canView) {
            emptyPosts.setText("No posts yet");
            attachPostsListener();
        } else {
            detachPostsListener();
            postsAdapter.setItems(new ArrayList<>());
            showPostsLoading(false);
            emptyPosts.setText("Follow to see posts");
            emptyPosts.setVisibility(View.VISIBLE);
            userPostsRecycler.setVisibility(View.INVISIBLE);
        }
    }

    private void attachPostsListener() {
        if (postsListener != null) return;
        showPostsLoading(true);
        postsQuery = FirebaseDatabase.getInstance(DB_URL)
                .getReference("posts")
                .orderByChild("uid")
                .equalTo(userId);

        postsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userPosts.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    blogItem item = child.getValue(blogItem.class);
                    if (item != null) {
                        item.setKey(child.getKey());
                        userPosts.add(item);
                    }
                }
                Collections.sort(userPosts, Comparator.comparingLong(blogItem::getTimestamp).reversed());
                postsAdapter.setItems(userPosts);
                showPostsLoading(false);
                emptyPosts.setVisibility(userPosts.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                showPostsLoading(false);
                Toast.makeText(UserProfileActivity.this, "Failed to load posts", Toast.LENGTH_SHORT).show();
            }
        };

        postsQuery.addValueEventListener(postsListener);
    }

    private void detachPostsListener() {
        if (postsQuery != null && postsListener != null) {
            postsQuery.removeEventListener(postsListener);
        }
        postsQuery = null;
        postsListener = null;
    }

    private void showPostsLoading(boolean loading) {
        postsProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        userPostsRecycler.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
    }
}
