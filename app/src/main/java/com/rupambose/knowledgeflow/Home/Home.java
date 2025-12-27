package com.rupambose.knowledgeflow.Home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rupambose.knowledgeflow.Home.adapter.RecyclerViewAdapter;
import com.rupambose.knowledgeflow.Home.adapter.blogItem;
import com.rupambose.knowledgeflow.MainProfile;
import com.rupambose.knowledgeflow.R;

import java.util.Arrays;
import java.util.List;

public class Home extends AppCompatActivity {

    ImageView cover;
    FloatingActionButton floating_add,floating_writing,floating_question,floating_question_answer,floating_bookmarks;
    Animation animation_rotate_open_anim,animation_rotate_close_anim,animation_from_bottom,animation_to_bottom;
    boolean  clicked = true;

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

        animation_rotate_open_anim = AnimationUtils.loadAnimation(this,R.anim.rotate_open_anim);
        animation_rotate_close_anim = AnimationUtils.loadAnimation(this,R.anim.rotate_close_anim);
        animation_from_bottom = AnimationUtils.loadAnimation(this,R.anim.from_bottom);
        animation_to_bottom = AnimationUtils.loadAnimation(this,R.anim.to_bottom);

        String userId = FirebaseAuth.getInstance().getUid();

        floating_writing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this,com.rupambose.knowledgeflow.BlogWriting.WriteBlogActivity.class);
                startActivity(intent);
            }
        });


        DatabaseReference ref = FirebaseDatabase.getInstance("https://knowledge-flow-87853-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Users")
                .child(userId)
                .child("profileImage");

        ref.get().addOnSuccessListener(snapshot -> {
            String url = snapshot.getValue(String.class);
            if (url != null && !url.isEmpty()) {
                Glide.with(this).load(url).into(cover);
            }
        });

        floating_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                SetVisibility(clicked);
                SetAnimation(clicked);
                clicked = !clicked;
            }
        });

        cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, MainProfile.class);
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.blogRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this);
        recyclerView.setAdapter(adapter);

        List<blogItem> mockItems = Arrays.asList(
                new blogItem("Design a better blog card", "https://picsum.photos/200", "Rupam", "Dec 24, 2025", "This is a sample blog content preview. It gives a brief overview of the blog post to entice readers to click and read more.", 24, 5),
                new blogItem("Jetpack tips", "https://picsum.photos/201", "Alex", "Dec 20, 2025", "Compose and Views can live together. Here is how to do it elegantly.", 12, 3)
        );
        adapter.setItems(mockItems);
    }

    private void SetVisibility(boolean clicked){
        if(clicked){
            floating_writing.setVisibility(View.VISIBLE);
            floating_question_answer.setVisibility(View.VISIBLE);
            floating_question.setVisibility(View.VISIBLE);
            floating_bookmarks.setVisibility(View.VISIBLE);

        }
        else{
            floating_writing.setVisibility(View.INVISIBLE);
            floating_question_answer.setVisibility(View.INVISIBLE);
            floating_question.setVisibility(View.INVISIBLE);
            floating_bookmarks.setVisibility(View.INVISIBLE);
        }
    }
    private void SetAnimation(boolean clicked){
        if(clicked){
            floating_writing.startAnimation(animation_from_bottom);
            floating_question.startAnimation(animation_from_bottom);
            floating_question_answer.startAnimation(animation_from_bottom);
            floating_bookmarks.startAnimation(animation_from_bottom);
            floating_add.startAnimation(animation_rotate_open_anim);
        }
        else{
            floating_writing.startAnimation(animation_to_bottom);
            floating_question_answer.startAnimation(animation_to_bottom);
            floating_question.startAnimation(animation_to_bottom);
            floating_bookmarks.startAnimation(animation_to_bottom);
            floating_add.startAnimation(animation_rotate_close_anim);
        }
    }
}