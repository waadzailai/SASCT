package com.example.sasct.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sasct.EditOfferActivity;
import com.example.sasct.Login;
import com.example.sasct.OrderActivity;
import com.example.sasct.ProfilEditActivity;
import com.example.sasct.R;
import com.example.sasct.SittingActivity;
import com.example.sasct.ViewOfferBuyerActivity;
import com.example.sasct.databinding.FragmentHomeBinding;
import com.example.sasct.db.DatabaseHelper;
import android.text.TextWatcher;

import androidx.recyclerview.widget.GridLayoutManager;

import com.example.sasct.model.Offer;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import java.util.List;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private DatabaseHelper dbHelper;
    private OfferAdapter offerAdapter;
    private RecyclerView offerRecyclerView;
    private EditText searchEditText;
    private MenuItem logout;

    private ImageButton imageButtonSettings;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        dbHelper = new DatabaseHelper(getContext());
        offerRecyclerView = binding.offerRecyclerView;
        logout = root.findViewById(R.id.logout);
        imageButtonSettings = root.findViewById(R.id.imageButtonSettings);

        imageButtonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

        offerAdapter = new OfferAdapter();
        offerRecyclerView.setAdapter(offerAdapter);
        offerRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Initialize EditText
        searchEditText = root.findViewById(R.id.search);

        // Initialize OfferAdapter
        offerAdapter = new HomeFragment.OfferAdapter();
        offerRecyclerView.setAdapter(offerAdapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // This can remain empty
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // This can remain empty
            }

            @Override
            public void afterTextChanged(Editable s) {
                String searchText = s.toString().trim();
                if (searchText.isEmpty()) {
                    loadAllOffers();  // Load all offers when search is cleared
                } else {
                    searchOffers(searchText);  // Perform search with the non-empty query
                }
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllOffers();  // Refresh data every time the fragment is shown
    }

    private void loadAllOffers() {
        dbHelper.getAllOffers(new DatabaseHelper.OnOffersRetrievedListener() {
            @Override
            public void onOffersRetrieved(List<Offer> offers) {
                offerAdapter.setOffers(offers);
            }
        });
    }

    private void searchOffers(String title) {
        dbHelper.searchOffers(title.toString().trim(), null, new DatabaseHelper.OnOffersRetrievedListener() {
            @Override
            public void onOffersRetrieved(List<Offer> offers) {
                offerAdapter.setOffers(offers);
            }
        });
    }

    public void showPopupMenu(View view) {
        // Creating a PopupMenu
        PopupMenu popup = new PopupMenu(getContext(), view);
        // Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.account_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == R.id.profile) {
                    // Handle Option Profile
                    Intent intent = new Intent(getActivity(), ProfilEditActivity.class);
                    startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.orders) {
                    // Navigate to OrderActivity
                    Intent intent = new Intent(getContext(), OrderActivity.class);
                    startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.sittings) {
                    // Handle Option Sittings
                    Intent intent = new Intent(getContext(), SittingActivity.class);
                    startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.logout) {
                    // Handle Option Logout
                    signOutUser();
                    return true;
                } else {
                    return false;
                }
            }
        });


        popup.show();
    }

    private void signOutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), Login.class);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish(); // Correct way to finish the Activity from Fragment
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class OfferAdapter extends RecyclerView.Adapter<HomeFragment.OfferAdapter.OfferViewHolder> {
        private List<Offer> offers;

        public void setOffers(List<Offer> offers) {
            this.offers = offers;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public HomeFragment.OfferAdapter.OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.offer_item, parent, false);
            return new HomeFragment.OfferAdapter.OfferViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HomeFragment.OfferAdapter.OfferViewHolder holder, int position) {
            Offer offer = offers.get(position);
            holder.bind(offer);
        }

        @Override
        public int getItemCount() {
            return offers != null ? offers.size() : 0;
        }

        class OfferViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView, priceTextView, quantityTextView, categoryTextView;
            ImageView offerImageView;

            OfferViewHolder(View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.titleTextView);
                priceTextView = itemView.findViewById(R.id.priceTextView);
                quantityTextView = itemView.findViewById(R.id.quantityTextView);
                categoryTextView = itemView.findViewById(R.id.categoryTextView);
                offerImageView = itemView.findViewById(R.id.offerImageView);
            }

            void bind(Offer offer) {
                titleTextView.setText(offer.getTitle());
                priceTextView.setText(String.format("Price: %.2f", offer.getPrice()));
                quantityTextView.setText(String.format("Quantity: %d", offer.getQuantity()));
                categoryTextView.setText(String.format("Category: %s", offer.getCategory()));
                if (offer.getImageUrl() != null && !offer.getImageUrl().isEmpty()) {
                    Picasso.get().load(offer.getImageUrl()).into(offerImageView);
                }

                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), ViewOfferBuyerActivity.class);
                    intent.putExtra("offer", offer);
                    startActivity(intent);
                });
            }
        }
    }


}