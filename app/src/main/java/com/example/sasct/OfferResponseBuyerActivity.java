package com.example.sasct;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.sasct.R;
import com.example.sasct.db.CartManager;
import com.example.sasct.db.DatabaseHelper;
import com.example.sasct.model.NegotiatedOffer;
import com.example.sasct.model.Offer;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class OfferResponseBuyerActivity extends AppCompatActivity {

    private NegotiatedOffer nogetiatedOffer;

    private TextView initialPriceView, initialQuantityView, currentStatusView;
    private EditText negotiatedPriceInput, negotiatedQuantityInput;
    private TextView offerDetails;
    private EditText editStatus;
    private Button updateStatusButton, addToCartButton;
    private CartManager cartManager;
    private double newPrice;
    private int newQuantity;

    private TextView titleView, descriptionView, categoryView, shipmentView;
    private TextView emailTextView, companyNameTextView;
    private ImageView userImageView, offerImageView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_response_buyer);
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

        emailTextView = findViewById(R.id.emailTextView);
        companyNameTextView = findViewById(R.id.companyNameTextView);
        userImageView = findViewById(R.id.userImageView);

        titleView = findViewById(R.id.titleView);
        descriptionView = findViewById(R.id.descriptionView);
        categoryView = findViewById(R.id.categoryView);
        shipmentView = findViewById(R.id.shipmentView);
        offerImageView = findViewById(R.id.offerImageView);
        updateStatusButton = findViewById(R.id.updateStatusButton);
        addToCartButton = findViewById(R.id.addToCartButton);
        cartManager = new CartManager();

        nogetiatedOffer = (NegotiatedOffer) getIntent().getSerializableExtra("offer");

        // Add to cart button (show only if offer is accepted)
        if (nogetiatedOffer.getStatus().equalsIgnoreCase("Accepted")) {
            addToCartButton.setVisibility(View.VISIBLE);
            addToCartButton.setOnClickListener(v -> addOfferToCart());
        } else {
            addToCartButton.setVisibility(View.GONE);
        }

        // Populate views
        if (nogetiatedOffer != null) {
            retrieveOfferData(nogetiatedOffer); // Retrieve details

            initialPriceView.setText(getString(R.string.initial_price, nogetiatedOffer.getInitialPrice()));
            initialQuantityView.setText(getString(R.string.initial_quantity, nogetiatedOffer.getInitialQuantity()));
            currentStatusView.setText(getString(R.string.current_status, nogetiatedOffer.getStatus()));


            negotiatedPriceInput.setText(String.valueOf(nogetiatedOffer.getNegotiatedPrice()));
            negotiatedQuantityInput.setText(String.valueOf(nogetiatedOffer.getNegotiatedQuantity()));

            updateStatusButton.setOnClickListener(v -> {
                try {
                    double newPrice = Double.parseDouble(negotiatedPriceInput.getText().toString());
                    int newQuantity = Integer.parseInt(negotiatedQuantityInput.getText().toString());

                    updateOfferDetails(newPrice, newQuantity, "Pending");
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid input for price or quantity", Toast.LENGTH_SHORT).show();
                }
            });
            addToCartButton.setOnClickListener(view -> addOfferToCart());
        }

    }

    private void updateOfferDetails(double newPrice, int newQuantity, String newStatus) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("negotiated_offers")
                .document(nogetiatedOffer.getFirestoreId())
                .update("negotiatedPrice", newPrice, "negotiatedQuantity", newQuantity, "status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(OfferResponseBuyerActivity.this, "Offer edited", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Notify the parent activity of success
                    finish(); // Finish the current activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OfferResponseBuyerActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateOfferStatus(String newStatus) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("negotiated_offers")
                .document(nogetiatedOffer.getFirestoreId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Status updated", Toast.LENGTH_SHORT).show();
                    nogetiatedOffer.setStatus(newStatus);

                    initialPriceView.setText(getString(R.string.initial_price, nogetiatedOffer.getInitialPrice()));
                    initialQuantityView.setText(getString(R.string.initial_quantity, nogetiatedOffer.getInitialQuantity()));
                    currentStatusView.setText(getString(R.string.current_status, nogetiatedOffer.getStatus()));

                    if (nogetiatedOffer.getStatus().equalsIgnoreCase("Accepted")) {
                        if (addToCartButton != null) {
                            addToCartButton.setVisibility(View.VISIBLE);
                        } else {
                            Log.e("OfferResponseBuyerActivity", "addToCartButton is null");
                        }

                    } else {
                        if (addToCartButton != null) {
                            addToCartButton.setVisibility(View.GONE);
                        } else {
                            Log.e("OfferResponseBuyerActivity", "addToCartButton is null");
                        }

                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
                            retrieveUserData(negotiatedOffer.getSupplierId()); // Fetch user data
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

        updateStatusButton.setOnClickListener(v -> {
            try {
                double newPrice = Double.parseDouble(negotiatedPriceInput.getText().toString());
                int newQuantity = Integer.parseInt(negotiatedQuantityInput.getText().toString());

                updateOfferDetails(newPrice, newQuantity, "Pending");
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid input for price or quantity", Toast.LENGTH_SHORT).show();
            }
        });
        addToCartButton.setOnClickListener(view -> addOfferToCart());
    }

    private void populateUserDetails(Map<String, Object> userData) {
        emailTextView.setText("Email: " + (String) userData.get("email"));
        companyNameTextView.setText("Company: " + (String) userData.get("companyName"));

        String imageUrl = (String) userData.get("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).into(userImageView);
        }
    }


    private void addOfferToCart() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Retrieve the original offer from the offers collection by offerId
        db.collection("offers")
                .document(nogetiatedOffer.getOfferId()) // Assuming offerId is the document ID
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Offer originalOffer = documentSnapshot.toObject(Offer.class);

                        // Check if the original offer is valid
                        if (originalOffer != null) {
                            // Create a new offer based on the original offer but with the negotiated price and quantity
                            Offer newOffer = new Offer(
                                    nogetiatedOffer.getBuyerId(), // Or supplierId, depending on the buyer/supplier roles
                                    originalOffer.getTitle(),
                                    originalOffer.getDescription(),
                                    originalOffer.getCategory(),
                                    originalOffer.getShipment(),
                                    nogetiatedOffer.getNegotiatedPrice(), // Use negotiated price
                                    nogetiatedOffer.getNegotiatedQuantity(), // Use negotiated quantity
                                    originalOffer.getImageUrl(),
                                    "p"
                            );

                            // Add this new offer to the offers collection or cart
                            db.collection("offers")
                                    .add(newOffer)
                                    .addOnSuccessListener(documentReference -> {
                                        // Offer added successfully
                                        String offerId = documentReference.getId();
                                        newOffer.setId(offerId); // Set the generated ID

                                        if (nogetiatedOffer.getBuyerId() != null && newOffer != null) {
                                            cartManager.addOfferToCart(nogetiatedOffer.getBuyerId(), newOffer, new CartManager.OnCartActionListener() {
                                                @Override
                                                public void onActionSuccess() {
                                                    // Notify user or update UI appropriately
                                                    Toast.makeText(OfferResponseBuyerActivity.this, "Added to cart!", Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onActionFailure(String errorMessage) {
                                                    // Handle failure case
                                                    Toast.makeText(OfferResponseBuyerActivity.this, "Error: Unable to add to cart.", Toast.LENGTH_SHORT).show();

                                                }
                                            });
                                        } else {
                                            // Handle case where userId or offer is not available
                                            Toast.makeText(this, "Error: Unable to add to cart.", Toast.LENGTH_SHORT).show();
                                        }

                                        Toast.makeText(this, "Offer added to cart", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error adding offer to cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "Error: Original offer not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error: Offer not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching original offer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
