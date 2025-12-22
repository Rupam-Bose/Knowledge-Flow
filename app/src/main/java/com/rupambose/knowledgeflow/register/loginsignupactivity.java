package com.rupambose.knowledgeflow.register;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rupambose.knowledgeflow.MainProfile;
import com.rupambose.knowledgeflow.Models.UserData;
import com.rupambose.knowledgeflow.R;

public class loginsignupactivity extends AppCompatActivity {


    private FirebaseAuth auth;
    private FirebaseDatabase database;
    String UserId;
    DatabaseReference UserRef ;
    UserData UserData;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loginsignupactivity);

        EditText login_Email = findViewById(R.id.emailEditText);
        EditText login_Pass = findViewById(R.id.passwardEditText);
        AppCompatButton login = findViewById(R.id.login_btn);
        EditText signUp_email = findViewById(R.id.SignUp_Email);
        EditText signUp_pass = findViewById(R.id.SignUp_Password);
        EditText signUp_name = findViewById(R.id.SignUp_Name);
        TextView txt = findViewById(R.id.textView4);
        AppCompatButton signUp = findViewById(R.id.signup_btn);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://knowledge-flow-87853-default-rtdb.asia-southeast1.firebasedatabase.app/");

        String action = getIntent().getStringExtra("Action");
        if("logIn".equals(action)){
            login_Email.setVisibility(View.VISIBLE);
            login_Pass.setVisibility(View.VISIBLE);
            login.setVisibility(View.VISIBLE);
            txt.setVisibility(View.GONE);
            signUp.setVisibility(View.GONE);
            signUp_email.setVisibility(View.GONE);
            signUp_name.setVisibility(View.GONE);
            signUp_pass.setVisibility(View.GONE);

            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String login_email = login_Email.getText().toString();
                    String login_pass = login_Pass.getText().toString();

                    if(login_email.isEmpty() || login_pass.isEmpty()){
                        Toast.makeText(loginsignupactivity.this, "Please Enter all the informations", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        auth.signInWithEmailAndPassword(login_email,login_pass)
                        .addOnCompleteListener(task ->{
                            if(task.isSuccessful()){
                                Toast.makeText(loginsignupactivity.this, "Successfully Login", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(loginsignupactivity.this, MainProfile.class);
                                startActivity(intent);
                                finish();
                            }
                            else{
                                Toast.makeText(loginsignupactivity.this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
        else if("signUp".equals(action)){
            login_Email.setVisibility(View.GONE);
            login_Pass.setVisibility(View.GONE);
            login.setVisibility(View.GONE);
            txt.setVisibility(View.GONE);
            signUp.setVisibility(View.VISIBLE);
            signUp_email.setVisibility(View.VISIBLE);
            signUp_name.setVisibility(View.VISIBLE);
            signUp_pass.setVisibility(View.VISIBLE);

            signUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){

                    String register_name = signUp_name.getText().toString();
                    String register_email = signUp_email.getText().toString();
                    String register_pass = signUp_pass.getText().toString();

                    if(register_name.isEmpty() || register_pass.isEmpty() || register_email.isEmpty()){
                        Toast.makeText(loginsignupactivity.this, "Please Fill all the information", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        auth.createUserWithEmailAndPassword(register_email, register_pass)
                                .addOnCompleteListener(task -> {

                                    if (task.isSuccessful()) {

                                        saveUserToDatabase(register_name, register_email);

                                    } else {

                                        auth.signInWithEmailAndPassword(register_email, register_pass)
                                                .addOnCompleteListener(loginTask -> {

                                                    if (loginTask.isSuccessful()) {
                                                        saveUserToDatabase(register_name, register_email);
                                                    } else {
                                                        Toast.makeText(
                                                                loginsignupactivity.this,
                                                                loginTask.getException().getMessage(),
                                                                Toast.LENGTH_LONG
                                                        ).show();
                                                    }
                                                });
                                    }
                                });


                    }

                }
            });


        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void saveUserToDatabase(String name, String email) {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Log.e("DB_TEST", "User is NULL");
            return;
        }

        String userId = user.getUid();
        Log.d("DB_TEST", "UID: " + userId);

        DatabaseReference userRef = database.getReference("Users").child(userId);

        UserData userData = new UserData(name, email);

        userRef.setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Successfully Registered", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(loginsignupactivity.this, welcome.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("DB_TEST", "DB ERROR: " + e.getMessage());
                });
    }

}