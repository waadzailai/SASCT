package com.example.sasct;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ThanksActivity extends AppCompatActivity {

    private Button backToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        // Set the layout for the activity before initializing components
        setContentView(R.layout.activity_thanks);

        // Initialize the button
        backToHome = findViewById(R.id.backToHome);

        // Set the click listener for the button
        backToHome.setOnClickListener(view -> navigateToMainActivity(view));
    }

    private void navigateToMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent); // Start the new activity
        finish(); // End the current activity
    }

}