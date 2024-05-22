package com.example.sasct;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.sasct.db.DatabaseHelper;
import com.example.sasct.model.NegotiatedOffer;
import com.example.sasct.model.Offer;
import com.google.firebase.firestore.FirebaseFirestore;

public class NegotiationActivity extends AppCompatActivity {

    private EditText specifyPrice, quantityNegotiation;
    private Button btnSendNegotiation, btnCancel;
    private Offer offer;
    private DatabaseHelper databaseHelper;
    private Toolbar toolbar;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_negotiation);

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.title_activity_negotitiate_offer);

        ImageView backButton = toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> this.onBackPressed());

        // Initialize views
        specifyPrice = findViewById(R.id.specifyPrice);
        quantityNegotiation = findViewById(R.id.quantityNegotiation);
        btnSendNegotiation = findViewById(R.id.btnSendNegotiation);
        btnCancel = findViewById(R.id.btnCancel);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Retrieve the Offer object from the intent
        offer = (Offer) getIntent().getSerializableExtra("offer");

        // Populate initial views
        if (offer != null) {
            specifyPrice.setText(String.valueOf(offer.getPrice()));
            quantityNegotiation.setText(String.valueOf(offer.getQuantity()));
        }
        // Handle Send button click
        btnSendNegotiation.setOnClickListener(view -> {
            if (isValidInput(specifyPrice.getText().toString(), quantityNegotiation.getText().toString())) {
                double newPrice = Double.parseDouble(specifyPrice.getText().toString());
                int newQuantity = Integer.parseInt(quantityNegotiation.getText().toString());

                if (newPrice == offer.getPrice() && newQuantity == offer.getQuantity()) {
                    Toast.makeText(NegotiationActivity.this, "You have to choose different values from the original.", Toast.LENGTH_LONG).show();
                } else if (offer != null) {
                    NegotiatedOffer negotiatedOffer = new NegotiatedOffer(
                            offer.getId(),
                            databaseHelper.getCurrentUserId(), // Buyer ID
                            offer.getUserId(), // Supplier ID
                            offer.getPrice(),
                            offer.getQuantity(),
                            newPrice,
                            newQuantity,
                            "Pending"
                    );

                    saveNegotiatedOffer(negotiatedOffer);
                }
            } else {
                Toast.makeText(NegotiationActivity.this, "Please fill in both price and quantity.", Toast.LENGTH_LONG).show();
            }
        });

        // Handle Cancel button click
        btnCancel.setOnClickListener(view -> finish());
    }

    private boolean isValidInput(String price, String quantity) {
        try {
            double p = Double.parseDouble(price);
            int q = Integer.parseInt(quantity);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void saveNegotiatedOffer(NegotiatedOffer negotiatedOffer) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Check if a negotiation already exists for this user and offer
        db.collection("negotiated_offers")
            .whereEqualTo("buyerId", negotiatedOffer.getBuyerId())
            .whereEqualTo("offerId", negotiatedOffer.getOfferId())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    // If there are documents, the user already negotiated this offer
                    Toast.makeText(NegotiationActivity.this, "You have already negotiated this offer.", Toast.LENGTH_LONG).show();
                } else {
                    // No existing negotiation, proceed to create a new one
                    createNewNegotiation(negotiatedOffer, db);
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(NegotiationActivity.this, "Error checking existing negotiations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void createNewNegotiation(NegotiatedOffer negotiatedOffer, FirebaseFirestore db) {
        db.collection("negotiated_offers")
            .add(negotiatedOffer)
            .addOnSuccessListener(documentReference -> {
                // Update the Firestore-assigned ID in the document itself
                db.collection("negotiated_offers")
                    .document(documentReference.getId())
                    .update("firestoreId", documentReference.getId())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(NegotiationActivity.this, "Negotiation submitted!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(NegotiationActivity.this, "Error updating Firestore ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(NegotiationActivity.this, "Error saving document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

}