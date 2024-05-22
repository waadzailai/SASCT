package com.example.sasct;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.sasct.db.DatabaseHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.example.sasct.adapters.CardAdapter;
import com.example.sasct.model.CreditCard;
import com.example.sasct.model.Address;
import com.example.sasct.model.CartItem;
import com.example.sasct.model.Order;

public class EditCardActivity extends AppCompatActivity {
    private EditText cardNumberEditText;
    private EditText cardNameEditText;
    private EditText expirationEditText;
    private EditText cvvEditText;
    private DatabaseHelper databaseHelper;
    private String cardId, userId; // Track ID for updates
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_card); // Ensure this is the correct layout resource
        getSupportActionBar().hide();

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.title_activity_add_payment);

        ImageView backButton = toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> this.onBackPressed());

        databaseHelper = new DatabaseHelper(this);
        userId = databaseHelper.getCurrentUserId();

        cardNumberEditText = findViewById(R.id.cardNumberEditText);
        cardNameEditText = findViewById(R.id.cardNameEditText);
        expirationEditText = findViewById(R.id.expirationEditText);
        cvvEditText = findViewById(R.id.cvvEditText);

        CreditCard card = (CreditCard) getIntent().getSerializableExtra("card");

        if (card != null) {
            populateCardFields(card);
            cardId = card.getId(); // Track ID for updates
        }

        Button saveButton = findViewById(R.id.saveButton); // Ensure this button exists
        saveButton.setOnClickListener(view -> saveOrUpdateCard());
    }

    private void populateCardFields(CreditCard card) {
        cardNumberEditText.setText(card.getCardNumber());
        cardNameEditText.setText(card.getCardName());
        expirationEditText.setText(card.getExpiration());
        cvvEditText.setText(card.getCvv());
    }

    private void saveOrUpdateCard() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CreditCard newCard = new CreditCard(
                cardId,
                cardNumberEditText.getText().toString(),
                cardNameEditText.getText().toString(),
                expirationEditText.getText().toString(),
                cvvEditText.getText().toString()
        );

        if (cardId == null) {
            // Adding a new card
            db.collection("users").document(userId).collection("cards")
                    .add(newCard)
                    .addOnSuccessListener(documentReference -> {
                        newCard.setId(documentReference.getId()); // Set card ID to Firestore document ID

                        documentReference.set(newCard)
                                .addOnSuccessListener(aVoid -> {
                                    setResult(RESULT_OK);
                                    finish(); // Complete the current activity
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure appropriately
                                });
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure appropriately
                    });
        } else {
            // Updating an existing card
            db.collection("users").document(userId).collection("cards")
                    .document(cardId).set(newCard)
                    .addOnSuccessListener(aVoid -> {
                        setResult(RESULT_OK);
                        finish(); // Complete the current activity
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure appropriately
                    });
        }
    }

}