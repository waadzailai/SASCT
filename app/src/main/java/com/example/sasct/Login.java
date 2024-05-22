package com.example.sasct;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private FirebaseAuth mAuth;
    EditText email,password;
    TextView signup;
    FirebaseUser currentUser;
    Button login;
    CustomProgressDialog dialog;
    private Button guestModeButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        email=findViewById(R.id.editTextEmail);
        password=findViewById(R.id.editTextPassword);
        signup=findViewById(R.id.signUpET);
        login=findViewById(R.id.sendBtn);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailText = email.getText().toString();
                String passwordText = password.getText().toString();

                if(!email.getText().toString().isEmpty()){
                    if(!password.getText().toString().isEmpty()){
                        currentUser = mAuth.getCurrentUser();
                        loginToApp(email.getText().toString(),password.getText().toString());
                    }else {
                        password.setError("Password Should not be empty");
                    }
                }else {
                    email.setError("Email Should not be empty");
                }
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewRegisterClicked();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            Toast.makeText(Login.this, "Already Logged In.", Toast.LENGTH_SHORT).show();
            intentToMain(); // Redirect to main activity
        }
    }

    private void checkExistingLogin() {
        if (currentUser != null) {
            Toast.makeText(Login.this, "Already Logged In.", Toast.LENGTH_SHORT).show();
            intentToMain(); // Redirect to main activity
        }
    }

    private void loginToApp(String email,String password) {
        dialog=new CustomProgressDialog("Wait","Your data is been verifying. Please wait..",Login.this);
        dialog.showDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            intentToMain();
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(Login.this, "Authentication failed."+task.getException(),
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                        dialog.dismisDialog();
                    }
                });
    }

    private void intentToMain() {
        startActivity(new Intent(Login.this, MainActivity.class));
        finish();
    }

    private void viewRegisterClicked(){
        startActivity(new Intent(Login.this,Register.class));
        finish();
    }


}