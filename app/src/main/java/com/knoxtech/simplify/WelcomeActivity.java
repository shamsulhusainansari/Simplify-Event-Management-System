package com.knoxtech.simplify;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        new Handler().postDelayed(() -> {
            if (user!=null){
                startActivity(new Intent(WelcomeActivity.this,EventActivity.class));
                finish();
            }else{
                Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(i);
            }
            finish();
        }, 5000);
    }
}