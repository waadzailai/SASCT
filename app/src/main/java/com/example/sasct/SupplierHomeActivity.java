package com.example.sasct;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sasct.db.DatabaseHelper;
import com.example.sasct.model.Offer;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SupplierHomeActivity extends AppCompatActivity {
    private static final int EDIT_OFFER_REQUEST_CODE = 1001;
    private DatabaseHelper databaseHelper;
    private RecyclerView offerRecyclerView;
    private OfferAdapter offerAdapter;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_home);
        // Hide the Top Label
        getSupportActionBar().hide();

        // Initialize DatabaseHelper instance
        databaseHelper = new DatabaseHelper(this);
        // Initialize RecyclerView
        offerRecyclerView = findViewById(R.id.offerRecyclerView);
        offerRecyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // Display offers in 2 columns

        searchEditText = findViewById(R.id.search);
        // Initialize OfferAdapter
        offerAdapter = new OfferAdapter();
        offerRecyclerView.setAdapter(offerAdapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    databaseHelper.searchOffers(s.toString().trim(), null, new DatabaseHelper.OnOffersRetrievedListener() {
                        @Override
                        public void onOffersRetrieved(List<Offer> offers) {
                            offerAdapter.setOffers(offers);
                        }
                    });
                } else {
                    retrieveOffers(); // Call to show all offers when search is cleared
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

    }

    // Method to retrieve offers from the database
    private void retrieveOffers() {
        databaseHelper.getOffersForCurrentUser(new DatabaseHelper.OnOffersRetrievedListener() {
            @Override
            public void onOffersRetrieved(List<Offer> offers) {
                // Update the data set of the adapter with retrieved offers
                offerAdapter.setOffers(offers);
            }
        });
    }



    // Adapter class for RecyclerView
    private class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {
        private List<Offer> offers;

        public void setOffers(List<Offer> offers) {
            this.offers = offers;
            notifyDataSetChanged(); // Notify the adapter that the data set has changed
        }

        @NonNull
        @Override
        public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.offer_item, parent, false);
            return new OfferViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
            Offer offer = offers.get(position);
            holder.bind(offer);
        }

        @Override
        public int getItemCount() {
            return offers == null ? 0 : offers.size();
        }

        // ViewHolder class for the RecyclerView
        class OfferViewHolder extends RecyclerView.ViewHolder {
            private TextView titleTextView, priceTextView, quantityTextView, categoryTextView;
            private ImageView offerImageView;

            public OfferViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.titleTextView);
                priceTextView = itemView.findViewById(R.id.priceTextView);
                quantityTextView = itemView.findViewById(R.id.quantityTextView);
                offerImageView = itemView.findViewById(R.id.offerImageView);
                categoryTextView = itemView.findViewById(R.id.categoryTextView);
            }

            public void bind(Offer offer) {
                titleTextView.setText(offer.getTitle());
                priceTextView.setText("Price: " + offer.getPrice());
                quantityTextView.setText("Quantity: " + offer.getQuantity());
                categoryTextView.setText("Category: " + offer.getCategory());
                if (!offer.getImageUrl().isEmpty()) {
                    Picasso.get().load(offer.getImageUrl()).into(offerImageView);
                }

                // Set click listener
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Open EditOfferActivity and pass the offer data
                        Intent intent = new Intent(SupplierHomeActivity.this, EditOfferActivity.class);
                        intent.putExtra("offer", offer); // Pass the offer object to EditOfferActivity
                        startActivity(intent);
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_OFFER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String message = data.getStringExtra("message");
            // Offer updated or deleted successfully
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            // Refresh the offers list by retrieving them again from the database
            retrieveOffers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Retrieve offers from the database when the activity resumes
        retrieveOffers();
    }

}