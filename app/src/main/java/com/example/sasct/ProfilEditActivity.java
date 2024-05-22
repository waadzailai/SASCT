package com.example.sasct;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.sasct.db.DatabaseHelper;
import com.example.sasct.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilEditActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private String currentUserID;
    private DatabaseHelper databaseHelper;
    private User user;
    private CircleImageView imageViewer;
    private EditText companyName, email, userRole;
    private Button editProfileButton, doneButton;
    private Uri selectedImageUri;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getSupportActionBar().hide();

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.title_activity_profile);

        ImageView backButton = toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> this.onBackPressed());

        imageViewer = findViewById(R.id.imageViewer);
        companyName = findViewById(R.id.companyName);
        email = findViewById(R.id.email);
        userRole = findViewById(R.id.userRole);
        editProfileButton = findViewById(R.id.editProfile);
        doneButton = findViewById(R.id.doneButton);

        databaseHelper = new DatabaseHelper(this);
        currentUserID = databaseHelper.getCurrentUserId();

        fetchUserData();
        setButtonListeners();
    }

    private void fetchUserData() {
        databaseHelper.getUserData(currentUserID, new DatabaseHelper.OnUserRetrievedListener() {
            @Override
            public void onUserRetrieved(User userObject) {
                if (userObject != null) {
                    user = userObject;
                    updateUIWithUserData(user);
                }
            }

            @Override
            public void onUserRetrievalFailed(String errorMessage) {
                Log.e(TAG, "Failed to retrieve user data: " + errorMessage);
                Toast.makeText(ProfilEditActivity.this, "Failed to retrieve user data: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setButtonListeners() {
        findViewById(R.id.imagePicker).setOnClickListener(v -> openImagePicker());
        editProfileButton.setOnClickListener(v -> updateUserProfile());
        doneButton.setOnClickListener(v -> finish());
    }

    private void updateUIWithUserData(User user) {
        companyName.setText(user.getCompanyName());
        email.setText(user.getEmail());
        userRole.setText(user.getRole());

        if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
            Picasso.get().load(user.getImageUrl()).placeholder(R.drawable.image_edit_profil)
                    .error(R.drawable.image_edit_profil).into(imageViewer);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imageViewer.setImageURI(selectedImageUri);
        } else {
            Toast.makeText(this, "You didn't pick an image", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserProfile() {
        if (user == null) {
            Toast.makeText(this, "User data is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean shouldUpdate = false;
        if (!companyName.getText().toString().trim().isEmpty()) {
            user.setCompanyName(companyName.getText().toString().trim());
            shouldUpdate = true;
        }
        if (!email.getText().toString().trim().isEmpty()) {
            user.setEmail(email.getText().toString().trim());
            shouldUpdate = true;
        }
        if (selectedImageUri != null) {
            user.setImageUrl(selectedImageUri.toString());
            shouldUpdate = true;
        }

        if (shouldUpdate) {
            databaseHelper.updateUserData(user, selectedImageUri);
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No changes detected", Toast.LENGTH_SHORT).show();
        }
    }
}