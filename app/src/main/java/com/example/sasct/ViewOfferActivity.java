package com.example.sasct;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sasct.model.Offer;
import com.squareup.picasso.Picasso;
import com.example.sasct.db.DatabaseHelper;

import java.util.List;

public class ViewOfferActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    private LinearLayout offerContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_view_offer_supplier);

        // Initialize DatabaseHelper instance
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
//        offerContainer = findViewById(R.id.offerContainer);

        // Retrieve offers from Firestore
        retrieveOffers();
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
                    View offerView = LayoutInflater.from(ViewOfferActivity.this).inflate(R.layout.offer_item, null);

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
                            Intent intent = new Intent(ViewOfferActivity.this, EditOfferActivity.class);
                            intent.putExtra("offer", offer); // Pass the offer object to EditOfferActivity
                            startActivity(intent);
                        }
                    });

                }
            }
        });
    }

}
