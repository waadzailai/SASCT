package com.example.sasct;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sasct.model.Offer;
import com.example.sasct.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Register extends AppCompatActivity {

    EditText email,password,confirmPassword, companyName;
    RadioButton manager,customer;
    Button register;
    TextView alreadyAccount;
    private FirebaseAuth mAuth;
    CustomProgressDialog dialog;

    // to make user select his Role
    Button buyerRadioButton, supplierRadioButton;

    private static boolean isBuyer = false, isSupplier = false;
    private String selectedRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        companyName=findViewById(R.id.companyName);
        confirmPassword=findViewById(R.id.confirmPassword);
        register=findViewById(R.id.register);
        alreadyAccount=findViewById(R.id.alreadyAccount);


        alreadyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLogin();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validation();
            }
        });
    }
    private void validation() {
        if (email.getText().toString().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            email.setError("Invalid email address");
            return;
        }

        if (password.getText().toString().isEmpty() || password.getText().toString().length() < 8) {
            password.setError("Password must be at least 8 characters long");
            return;
        }

        if (!confirmPassword.getText().toString().equals(password.getText().toString())) {
            confirmPassword.setError("Passwords do not match");
            return;
        }

        if (selectedRole == null) {
            Toast.makeText(this, "Please select a role (buyer/supplier)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (companyName.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill in your company name", Toast.LENGTH_SHORT).show();
            return;
        }

        registration(email.getText().toString(), password.getText().toString(), selectedRole, companyName.getText().toString());
    }

    private void registration(String email, String password, String role, String companyName) {
        dialog = new CustomProgressDialog("Wait", "Your data is being verified. Please wait..", Register.this);
        dialog.showDialog();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userId = firebaseUser.getUid();
                                String defaultRole = "Buyer"; // Default role if not provided

                                User newUser = new User(userId, email, role != null ? role : defaultRole, companyName);
                                updateUserRoleInFirestore(userId, newUser);
                            } else {
                                Toast.makeText(Register.this, "User registration failed: FirebaseUser is null", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (task.getException() instanceof FirebaseNetworkException) {
                                // Notify user of the network error
                                Toast.makeText(Register.this, "Network error, please check your internet connection and try again.", Toast.LENGTH_LONG).show();

                                // Optionally, retry the operation or prompt the user to retry
                            }
                            Toast.makeText(Register.this, "Failed to register: " + task.getException(), Toast.LENGTH_SHORT).show();
                            Log.e("Registration", "Failed to register", task.getException());
                        }
                        dialog.dismisDialog();
                    }
                });
    }


    private void updateUserRoleInFirestore(String userId, User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Add the user document to the "users" collection
        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "User role updated successfully");
                        navigateToLogin(); // Navigate to login screen after updating role
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Error updating user role", e);
                    }
                });
    }



    private void userRoleUpdate(String displayName) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();
            user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        navigateToLogin();
                    } else {
                        Toast.makeText(Register.this, "Exception: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void chooseBuyer(View view) {
        isBuyer = true;
        isSupplier = false;

        selectedRole = "buyer";
    }

    public void chooseSupplier(View view) {
        isBuyer = true;
        isSupplier = false;

        selectedRole = "supplier";
    }


    private void navigateToLogin() {
        startActivity(new Intent(Register.this,Login.class));
        finish();
    }
}