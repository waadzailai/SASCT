package com.example.sasct;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.sasct.model.NegotiatedOffer;
import com.example.sasct.model.Offer;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class OfferResponseActivity extends AppCompatActivity {
    private TextView initialPriceView, initialQuantityView, currentStatusView;
    private EditText negotiatedPriceInput, negotiatedQuantityInput;
    private Button btnAccept, btnReject, btnEdit;
    private NegotiatedOffer offer;
    private double newPrice;
    private int newQuantity;
    private TextView titleView,descriptionView, categoryView, shipmentView;
    private TextView emailTextView, companyNameTextView;
    private ImageView userImageView, offerImageView;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_response);
        getSupportActionBar().hide();

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.title_activity_buyer_offer_response);

        ImageView backButton = toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> this.onBackPressed());

        // Initialize views
        initialPriceView = findViewById(R.id.initialPriceView);
        initialQuantityView = findViewById(R.id.initialQuantityView);
        currentStatusView = findViewById(R.id.currentStatusView);
        negotiatedPriceInput = findViewById(R.id.negotiatedPriceInput);
        negotiatedQuantityInput = findViewById(R.id.negotiatedQuantityInput);
        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);
        btnEdit = findViewById(R.id.btnEdit);

        titleView = findViewById(R.id.titleView);
        descriptionView = findViewById(R.id.descriptionView);
        categoryView = findViewById(R.id.categoryView);
        shipmentView = findViewById(R.id.shipmentView);

        emailTextView = findViewById(R.id.emailTextView);
        companyNameTextView = findViewById(R.id.companyNameTextView);
        userImageView = findViewById(R.id.userImageView);
        offerImageView = findViewById(R.id.offerImageView);

        // Retrieve the offer
        offer = (NegotiatedOffer) getIntent().getSerializableExtra("offer");

        // Populate views
        if (offer != null) {
            retrieveOfferData(offer); // Retrieve details

            negotiatedPriceInput.setText(String.valueOf(offer.getNegotiatedPrice()));
            negotiatedQuantityInput.setText(String.valueOf(offer.getNegotiatedQuantity()));
            currentStatusView.setText(getString(R.string.current_status, offer.getStatus()));
        }
    }

    private void retrieveOfferData(NegotiatedOffer negotiatedOffer) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("offers")
                .document(negotiatedOffer.getOfferId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Offer originalOffer = documentSnapshot.toObject(Offer.class);

                        if (originalOffer != null) {
                            populateOfferDetails(originalOffer, negotiatedOffer);
                            retrieveUserData(negotiatedOffer.getBuyerId()); // Fetch user data
                        }
                    } else {
                        Toast.makeText(this, "Offer not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error retrieving offer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void retrieveUserData(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> userData = documentSnapshot.getData();

                        if (userData != null) {
                            populateUserDetails(userData);
                        }
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error retrieving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void populateOfferDetails(Offer offer, NegotiatedOffer negotiatedOffer) {
        initialPriceView.setText(getString(R.string.initial_price, negotiatedOffer.getInitialPrice()));
        initialQuantityView.setText(getString(R.string.initial_quantity, negotiatedOffer.getInitialQuantity()));


        titleView.setText(getString(R.string.offer_title, offer.getTitle()));
        descriptionView.setText(getString(R.string.offer_description, offer.getDescription()));
        categoryView.setText(getString(R.string.offer_category, offer.getCategory()));
        shipmentView.setText(getString(R.string.offer_shipment, offer.getShipment()));

        if (!offer.getImageUrl().isEmpty()) {
            Picasso.get().load(offer.getImageUrl()).into(offerImageView);
        }

        btnAccept.setOnClickListener(view -> updateOfferStatus("Accepted"));
        btnReject.setOnClickListener(view -> updateOfferStatus("Rejected"));
        btnEdit.setOnClickListener(v -> {
            try {
                newPrice = Double.parseDouble(negotiatedPriceInput.getText().toString());
                newQuantity = Integer.parseInt(negotiatedQuantityInput.getText().toString());
                updateOfferDetails(newPrice, newQuantity, "Edited");
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUserDetails(Map<String, Object> userData) {
        emailTextView.setText("Email: " + (String) userData.get("email"));
        companyNameTextView.setText("Company: " + (String) userData.get("companyName"));

        String imageUrl = (String) userData.get("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).into(userImageView);
        }
    }

    private void updateOfferStatus(String newStatus) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("negotiated_offers")
                .document(offer.getFirestoreId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Offer " + newStatus, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private void updateOfferDetails(double newPrice, int newQuantity, String newStatus) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("negotiated_offers")
                .document(offer.getFirestoreId())
                .update("negotiatedPrice", newPrice, "negotiatedQuantity", newQuantity, "status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Offer edited", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            updateOfferDetails(newPrice, newQuantity, "Edited");
        }
    }
}
