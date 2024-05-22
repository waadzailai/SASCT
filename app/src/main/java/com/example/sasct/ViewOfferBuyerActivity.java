package com.example.sasct;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.sasct.db.CartManager;
import com.example.sasct.db.DatabaseHelper;
import com.example.sasct.model.Offer;
import com.example.sasct.model.User;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ViewOfferBuyerActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private LinearLayout offerContainer;
    private ImageView imageView;
    private String imageUrl, userId;
    private TextView etTitle, etDescription, etPrice, etQuantity, categoryTextView, shipmentTextView ;
    private Button btnAddToCart, btnNegotiate;
    private Offer offer;
    private CartManager cartManager;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_offer_buyer);
        getSupportActionBar().hide();

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.title_activity_view_offer);

        ImageView backButton = toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Log.d("BackButton", "Back button pressed");
            onBackPressed();
        });

        // Initialize views
        imageView = findViewById(R.id.imageView);
        etTitle = findViewById(R.id.titleTextView);
        etDescription = findViewById(R.id.descriptionTextView);
        etPrice = findViewById(R.id.priceTextView);
        etQuantity = findViewById(R.id.quantityTextView);
        categoryTextView = findViewById(R.id.categoryTextView);
        shipmentTextView = findViewById(R.id.shipmentTextView);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnNegotiate = findViewById(R.id.btnNogitiate);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);
        userId = databaseHelper.getCurrentUserId();
        cartManager = new CartManager();
        // Retrieve the Offer object from the intent
        offer = (Offer) getIntent().getSerializableExtra("offer");

        // Prevent Supplier from adding items to cart
        databaseHelper.getUserData(userId, new DatabaseHelper.OnUserRetrievedListener() {
            @Override
            public void onUserRetrieved(User user) {
                runOnUiThread(() -> {
                    String userRole = user.getRole();
                    if ("supplier".equals(userRole)) {
                        btnAddToCart.setVisibility(View.INVISIBLE);
                        btnNegotiate.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onUserRetrievalFailed(String errorMessage) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Failed to retrieve user data: " + errorMessage);
                });
            }
        });

        // Populate the views with offer details
        if (offer != null) {
            imageUrl = offer.getImageUrl();
            if(imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get().load(imageUrl).into(imageView);
            }
            etTitle.setText(offer.getTitle());
            etDescription.setText(offer.getDescription());
            etPrice.setText(String.valueOf(offer.getPrice()));
            etQuantity.setText(String.valueOf(offer.getQuantity()));
            categoryTextView.setText(String.valueOf(offer.getCategory()));
            shipmentTextView.setText(String.valueOf(offer.getShipment()));
        }

        btnAddToCart.setOnClickListener(view -> {

            if (userId != null && offer != null) {
                cartManager.addOfferToCart(userId, offer, new CartManager.OnCartActionListener() {
                    @Override
                    public void onActionSuccess() {
                        // Notify user or update UI appropriately
                        Toast.makeText(ViewOfferBuyerActivity.this, "Added to cart!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onActionFailure(String errorMessage) {
                        // Handle failure case
                        Toast.makeText(ViewOfferBuyerActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Handle case where userId or offer is not available
                Toast.makeText(ViewOfferBuyerActivity.this, "Error: Unable to add to cart.", Toast.LENGTH_SHORT).show();
            }
        });

        btnNegotiate.setOnClickListener(view -> {
            if (offer != null) {
                Intent intent = new Intent(ViewOfferBuyerActivity.this, NegotiationActivity.class);
                intent.putExtra("offer", offer); // Pass the offer object
                startActivity(intent);
            } else {
                Toast.makeText(ViewOfferBuyerActivity.this, "Error: Unable to negotiate.", Toast.LENGTH_SHORT).show();
            }
        });


    }

    // Method to dynamically add offer view to the layout
    private void retrieveOffers() {
        // Clear existing views
        offerContainer.removeAllViews();

        // Retrieve offers using DatabaseHelper
        databaseHelper.getOffersForCurrentUser(new DatabaseHelper.OnOffersRetrievedListener() {
            @Override
            public void onOffersRetrieved(List<Offer> offers) {
                // Inflate offer_item.xml layout for each offer and add it to the container
                for (Offer offer : offers) {
                    View offerView = LayoutInflater.from(ViewOfferBuyerActivity.this).inflate(R.layout.offer_item, null);

                    TextView titleTextView = offerView.findViewById(R.id.titleTextView);
//                    TextView descriptionTextView = offerView.findViewById(R.id.descriptionTextView);
                    TextView priceTextView = offerView.findViewById(R.id.priceTextView);
                    TextView quantityTextView = offerView.findViewById(R.id.quantityTextView);
                    ImageView offerImageView = offerView.findViewById(R.id.offerImageView);

                    // Set offer data to TextViews
                    titleTextView.setText(offer.getTitle());
//                    descriptionTextView.setText(offer.getDescription());
                    priceTextView.setText("Price: " + offer.getPrice());
                    quantityTextView.setText("Quantity: " + offer.getQuantity());

                    // Load image into the ImageView using Picasso
                    if (!offer.getImageUrl().isEmpty()) {
                        Picasso.get().load(offer.getImageUrl()).into(offerImageView);
                    }

                    // Add click listener to offer view for editing/deleting
                    offerView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Open EditOfferActivity and pass the offer data
                            Intent intent = new Intent(ViewOfferBuyerActivity.this, EditOfferActivity.class);
                            intent.putExtra("offer", offer); // Pass the offer object to EditOfferActivity
                            startActivity(intent);
                        }
                    });

                    // Add offerView to the container
                    offerContainer.addView(offerView);
                }
            }
        });
    }
}
