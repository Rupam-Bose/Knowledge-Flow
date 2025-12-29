package com.rupambose.knowledgeflow;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rupambose.knowledgeflow.BlogWriting.BlogPost;
import com.rupambose.knowledgeflow.Home.BookmarksActivity;
import com.rupambose.knowledgeflow.Home.Home;
import com.rupambose.knowledgeflow.register.welcome;

import java.util.Map;

public class MainProfile extends AppCompatActivity {

    ImageView cover;
    CardView logout,Home,WritingBlog,bookmarks,notification;
    TextView name,email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_profile);

        FloatingActionButton fabicon = findViewById(R.id.camera_button);
        logout = findViewById(R.id.cardViewLogout);
        cover = findViewById(R.id.profile_image);
        name = findViewById(R.id.Username);
        email = findViewById(R.id.Useremail);
        Home = findViewById(R.id.cardViewHome);
        WritingBlog = findViewById(R.id.cardViewWritingBlog);
        bookmarks = findViewById(R.id.cardViewbookmarks);
        notification = findViewById(R.id.cardViewNotifications);
        String userId = FirebaseAuth.getInstance().getUid();

        WritingBlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainProfile.this,com.rupambose.knowledgeflow.BlogWriting.WriteBlogActivity.class);
                startActivity(intent);
            }
        });

        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainProfile.this, question_detail.class);
                startActivity(intent);
            }
        });

        Home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainProfile.this,com.rupambose.knowledgeflow.Home.Home.class);
                startActivity(intent);
            }
        });

        bookmarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainProfile.this, BookmarksActivity.class);
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

        DatabaseReference userRef = FirebaseDatabase
                .getInstance("https://knowledge-flow-87853-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Users")
                .child(userId);

        userRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) return;

            String uname = snapshot.child("name").getValue(String.class);
            String mail = snapshot.child("email").getValue(String.class);

            if (uname != null) name.setText(uname);
            if (mail != null) email.setText(mail);
        });

        fabicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(MainProfile.this)
                        .crop()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MainProfile.this, "Signed Out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainProfile.this, welcome.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            cover.setImageURI(uri);

            uploadToCloudinary(uri);
        }
    }

    private void uploadToCloudinary(Uri uri) {
        MediaManager.get().upload(uri)
                .unsigned("android_unsigned")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(MainProfile.this, "Uploading...", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {

                          String imageUrl = resultData.get("secure_url").toString();

                        String userId = FirebaseAuth.getInstance().getUid();

                          FirebaseDatabase.getInstance("https://knowledge-flow-87853-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                .getReference("Users")
                                .child(userId)
                                .child("profileImage")
                                .setValue(imageUrl);

                        Toast.makeText(MainProfile.this, "Uploaded & Saved!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(MainProfile.this, "Upload Failed: " + error.getDescription(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                })
                .dispatch();
    }
}
