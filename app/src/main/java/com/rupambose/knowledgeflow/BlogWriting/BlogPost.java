package com.rupambose.knowledgeflow.BlogWriting;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
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
import com.rupambose.knowledgeflow.Models.Comment;
import com.rupambose.knowledgeflow.R;
import com.rupambose.knowledgeflow.UserProfileActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BlogPost extends AppCompatActivity {

    private static final String DB_URL = "https://knowledge-flow-87853-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private ImageView profileImage;
    private TextView authorName;
    private TextView blogMeta;
    private TextView blogTitle;
    private TextView blogContent;
    private ImageView likeButton;
    private TextView likeCount;
    private TextView commentCount;
    private ImageView bookmarkButton;
    private RecyclerView commentsRecyclerView;
    private EditText commentInput;
    private ImageView commentSend;

    private String postId;
    private String authorUid;
    private boolean liked;
    private int likesValue;
    private boolean bookmarked;
    private final List<Comment> comments = new ArrayList<>();
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blog_post);

        bindViews();
        applyInsets();
        setupCommentsList();
        readIntent();
        prefillFromIntent();
        fetchLatestPost();
        wireLikeToggle();
        wireBookmarkToggle();
        wireSendComment();
    }

    private void bindViews() {
        profileImage = findViewById(R.id.profileImage);
        authorName = findViewById(R.id.authorName);
        blogMeta = findViewById(R.id.blogMeta);
        blogTitle = findViewById(R.id.blogTitle);
        blogContent = findViewById(R.id.blogContent);
        likeButton = findViewById(R.id.likeButton);
        likeCount = findViewById(R.id.likeCount);
        commentCount = findViewById(R.id.commentCount);
        bookmarkButton = findViewById(R.id.bookmarkButton);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentInput = findViewById(R.id.commentInput);
        commentSend = findViewById(R.id.commentSend);

        View.OnClickListener openAuthor = v -> {
            if (authorUid == null || authorUid.isEmpty()) return;
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("userId", authorUid);
            startActivity(intent);
        };
        profileImage.setOnClickListener(openAuthor);
        authorName.setOnClickListener(openAuthor);
    }

    private void applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void readIntent() {
        postId = getIntent().getStringExtra("postId");
        authorUid = getIntent().getStringExtra("authorUid");
        likesValue = getIntent().getIntExtra("likesCount", 0);
        liked = false;
    }

    private void prefillFromIntent() {
        blogTitle.setText(getIntent().getStringExtra("title"));
        blogContent.setText(getIntent().getStringExtra("content"));
        authorName.setText(getIntent().getStringExtra("profileName"));
        blogMeta.setText(getIntent().getStringExtra("date"));
        likeCount.setText(String.valueOf(likesValue));
        commentCount.setText(String.valueOf(getIntent().getIntExtra("commentsCount", 0)));

        String profilePic = getIntent().getStringExtra("profilePic");
        Glide.with(this)
                .load(profilePic)
                .placeholder(R.drawable.profileicon)
                .error(R.drawable.profileicon)
                .circleCrop()
                .into(profileImage);

        // initialize bookmark as false until fetched
        bookmarked = false;
    }

    private void fetchLatestPost() {
        if (postId == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance(DB_URL)
                .getReference("posts")
                .child(postId);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                String title = snapshot.child("contentTitle").getValue(String.class);
                String content = snapshot.child("post").getValue(String.class);
                String profilePic = snapshot.child("profilePic").getValue(String.class);
                String name = snapshot.child("profileName").getValue(String.class);
                String date = snapshot.child("date").getValue(String.class);
                Long likes = snapshot.child("likesCount").getValue(Long.class);
                Long comments = snapshot.child("commentsCount").getValue(Long.class);
                authorUid = snapshot.child("uid").getValue(String.class);

                if (title != null) blogTitle.setText(title);
                if (content != null) blogContent.setText(content);
                if (name != null) authorName.setText(name);
                if (date != null) blogMeta.setText(date);
                if (likes != null) {
                    likesValue = likes.intValue();
                    likeCount.setText(String.valueOf(likesValue));
                }
                if (comments != null) commentCount.setText(String.valueOf(comments));

                Glide.with(BlogPost.this)
                        .load(profilePic)
                        .placeholder(R.drawable.profileicon)
                        .error(R.drawable.profileicon)
                        .circleCrop()
                        .into(profileImage);

                fetchLikeState();
                fetchBookmarkState();
                listenForComments();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(BlogPost.this, "Failed to load post", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLikeState() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || postId == null) return;
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("posts")
                .child(postId)
                .child("likes")
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        liked = snapshot.exists();
                        likeButton.setImageResource(liked ? R.drawable.heart_red : R.drawable.heart_black);
                    }
                    @Override public void onCancelled(DatabaseError error) { }
                });
    }

    private void fetchBookmarkState() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || postId == null) return;
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("posts")
                .child(postId)
                .child("bookmarks")
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        bookmarked = snapshot.exists();
                        bookmarkButton.setImageResource(bookmarked ? R.drawable.save_filled : R.drawable.save);
                    }
                    @Override public void onCancelled(DatabaseError error) { }
                });
    }

    private void wireLikeToggle() {
        likeButton.setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null || postId == null) return;

            boolean currentlyLiked = liked;
            int newCount = Math.max(likesValue + (currentlyLiked ? -1 : 1), 0);

            DatabaseReference postRef = FirebaseDatabase.getInstance(DB_URL)
                    .getReference("posts")
                    .child(postId);

            if (currentlyLiked) {
                postRef.child("likes").child(uid).removeValue();
            } else {
                postRef.child("likes").child(uid).setValue(true);
            }
            postRef.child("likesCount").setValue(newCount);

            liked = !currentlyLiked;
            likesValue = newCount;
            likeCount.setText(String.valueOf(newCount));
            likeButton.setImageResource(liked ? R.drawable.heart_red : R.drawable.heart_black);
        });
    }

    private void wireBookmarkToggle() {
        bookmarkButton.setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null || postId == null) return;
            boolean current = bookmarked;
            DatabaseReference ref = FirebaseDatabase.getInstance(DB_URL)
                    .getReference("posts")
                    .child(postId)
                    .child("bookmarks")
                    .child(uid);
            if (current) {
                ref.removeValue();
            } else {
                ref.setValue(true);
            }
            bookmarked = !current;
            bookmarkButton.setImageResource(bookmarked ? R.drawable.save_filled : R.drawable.save);
        });
    }

    private void setupCommentsList() {
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(this, comments);
        commentsRecyclerView.setAdapter(commentAdapter);
        commentAdapter.setListener(this::toggleCommentLike);
    }

    private void wireSendComment() {
        commentSend.setOnClickListener(v -> {
            String text = commentInput.getText().toString().trim();
            if (TextUtils.isEmpty(text)) {
                commentInput.setError("Enter comment");
                return;
            }
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null || postId == null) {
                Toast.makeText(this, "Sign in first", Toast.LENGTH_SHORT).show();
                return;
            }
            DatabaseReference userRef = FirebaseDatabase.getInstance(DB_URL)
                    .getReference("Users").child(uid);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String name = snapshot.child("name").getValue(String.class);
                    String pic = snapshot.child("profileImage").getValue(String.class);
                    name = name != null ? name : "User";
                    pic = pic != null ? pic : "";

                    long ts = System.currentTimeMillis();
                    String date = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                            .format(new Date(ts));

                    DatabaseReference commentsRef = FirebaseDatabase.getInstance(DB_URL)
                            .getReference("posts").child(postId).child("comments");
                    String commentId = commentsRef.push().getKey();
                    if (commentId == null) return;

                    Comment c = new Comment();
                    c.id = commentId;
                    c.text = text;
                    c.uid = uid;
                    c.userName = name;
                    c.profilePic = pic;
                    c.timestamp = ts;
                    c.date = date;
                    c.likesCount = 0;

                    commentsRef.child(commentId).setValue(c);
                    FirebaseDatabase.getInstance(DB_URL)
                            .getReference("posts").child(postId)
                            .child("commentsCount").setValue(comments.size() + 1);
                    commentInput.setText("");
                }

                @Override public void onCancelled(DatabaseError error) { }
            });
        });
    }

    private void listenForComments() {
        if (postId == null) return;
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("posts").child(postId).child("comments")
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        comments.clear();
                        String uid = FirebaseAuth.getInstance().getUid();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Comment c = child.getValue(Comment.class);
                            if (c != null) {
                                c.id = child.getKey();
                                // populate liked flag
                                c.liked = child.child("likes").child(uid == null ? "" : uid).exists();
                                Long cnt = child.child("likesCount").getValue(Long.class);
                                if (cnt != null) c.likesCount = cnt.intValue();
                                comments.add(c);
                            }
                        }
                        commentAdapter.notifyDataSetChanged();
                        commentCount.setText(String.valueOf(comments.size()));
                    }
                    @Override public void onCancelled(DatabaseError error) { }
                });
    }

    private void toggleCommentLike(Comment c) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || postId == null || c.id == null) return;

        boolean currentlyLiked = c.liked;
        int newCount = Math.max(c.likesCount + (currentlyLiked ? -1 : 1), 0);

        DatabaseReference ref = FirebaseDatabase.getInstance(DB_URL)
                .getReference("posts").child(postId).child("comments").child(c.id);

        if (currentlyLiked) {
            ref.child("likes").child(uid).removeValue();
        } else {
            ref.child("likes").child(uid).setValue(true);
        }
        ref.child("likesCount").setValue(newCount);

        c.liked = !currentlyLiked;
        c.likesCount = newCount;
        commentAdapter.notifyDataSetChanged();
    }
}