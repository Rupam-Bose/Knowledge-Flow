package com.rupambose.knowledgeflow.register;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.rupambose.knowledgeflow.R;

public class loginsignupactivity extends AppCompatActivity {


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loginsignupactivity);

        EditText login_Name = findViewById(R.id.emailEditText);
        EditText login_Pass = findViewById(R.id.passwardEditText);
        AppCompatButton login = findViewById(R.id.login_btn);
        EditText signUp_email = findViewById(R.id.SignUp_Email);
        EditText signUp_pass = findViewById(R.id.SignUp_Password);
        EditText signUp_name = findViewById(R.id.SignUp_Name);
        TextView txt = findViewById(R.id.textView4);
        AppCompatButton signUp = findViewById(R.id.signup_btn);

        String action = getIntent().getStringExtra("Action");
        if("logIn".equals(action)){
            login_Name.setVisibility(View.VISIBLE);
            login_Pass.setVisibility(View.VISIBLE);
            login.setVisibility(View.VISIBLE);
            txt.setVisibility(View.VISIBLE);
            signUp.setVisibility(View.VISIBLE);
            signUp_email.setVisibility(View.GONE);
            signUp_name.setVisibility(View.GONE);
            signUp_pass.setVisibility(View.GONE);
        }
        else if("signUp".equals(action)){
            login_Name.setVisibility(View.GONE);
            login_Pass.setVisibility(View.GONE);
            login.setVisibility(View.GONE);
            txt.setVisibility(View.GONE);
            signUp.setVisibility(View.VISIBLE);
            signUp_email.setVisibility(View.VISIBLE);
            signUp_name.setVisibility(View.VISIBLE);
            signUp_pass.setVisibility(View.VISIBLE);

        }

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login_Name.setVisibility(View.GONE);
                login_Pass.setVisibility(View.GONE);
                login.setVisibility(View.GONE);
                txt.setVisibility(View.GONE);
                signUp.setVisibility(View.VISIBLE);
                signUp_email.setVisibility(View.VISIBLE);
                signUp_name.setVisibility(View.VISIBLE);
                signUp_pass.setVisibility(View.VISIBLE);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}