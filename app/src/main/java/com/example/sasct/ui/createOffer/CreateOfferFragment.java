package com.example.sasct.ui.createOffer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sasct.CreateOfferActivity;
import com.example.sasct.EditOfferActivity;
import com.example.sasct.R;
import com.example.sasct.db.DatabaseHelper;
import com.example.sasct.databinding.FragmentCreateOfferBinding;
import com.example.sasct.model.Offer;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CreateOfferFragment extends Fragment {
    private FragmentCreateOfferBinding binding;
    private DatabaseHelper databaseHelper;
    private RecyclerView offerRecyclerView;
    private OfferAdapter offerAdapter;
    private ImageButton addOfferButton;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateOfferBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        databaseHelper = new DatabaseHelper(getContext());

        // Set up RecyclerView
        offerRecyclerView = binding.offerRecyclerView;
        offerRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // Display in 2 columns

        offerAdapter = new OfferAdapter();
        offerRecyclerView.setAdapter(offerAdapter);

        // Load current user's offers
        loadUserOffers();

        addOfferButton = root.findViewById(R.id.addOfferButton); // Make sure this button exists

        if (addOfferButton != null) {
            addOfferButton.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), CreateOfferActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e("CreateOfferFragment", "addOfferButton not found");
        }

        return root;
    }

    private void loadUserOffers() {
        databaseHelper.getOffersForCurrentUser(new DatabaseHelper.OnOffersRetrievedListener() {
            @Override
            public void onOffersRetrieved(List<Offer> offers) {
                offerAdapter.setOffers(offers);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {
        private List<Offer> offers;

        public void setOffers(List<Offer> offers) {
            this.offers = offers;
            notifyDataSetChanged();
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

                itemView.setOnClickListener(v -> {
                    // Open EditOfferActivity or other relevant activity
                    Intent intent = new Intent(getContext(), EditOfferActivity.class);
                    intent.putExtra("offer", offer);
                    startActivity(intent);
                });
            }
        }
    }
}
