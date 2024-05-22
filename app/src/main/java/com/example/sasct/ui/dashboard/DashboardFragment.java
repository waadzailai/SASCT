package com.example.sasct.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sasct.OrderActivity;
import com.example.sasct.ProfilEditActivity;
import com.example.sasct.SittingActivity;
import com.example.sasct.databinding.FragmentDashboardBinding;

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
import com.example.sasct.R;
import com.example.sasct.databinding.FragmentHomeBinding;
import com.example.sasct.db.DatabaseHelper;
import android.text.TextWatcher;

import androidx.recyclerview.widget.GridLayoutManager;

import com.example.sasct.model.Offer;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import java.util.List;


public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    private DatabaseHelper dbHelper;
    private DashboardFragment.OfferAdapter offerAdapter;
    private RecyclerView offerRecyclerView;
    private EditText searchEditText;
    private MenuItem logout;
    private Spinner spinnerCategory;
    private String selectedCategory;
    private ImageButton imageButtonSettings;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize the Spinner and set up its adapter and listener
        spinnerCategory = root.findViewById(R.id.spinner_category);
        String[] categories = getResources().getStringArray(R.array.categories_array);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories[position];
                if (selectedCategory != null && !selectedCategory.equals("Select Category")) {
                    loadAllOffers(selectedCategory);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = categories[0];
            }
        });


        dbHelper = new DatabaseHelper(getContext());
        offerRecyclerView = binding.offerRecyclerView;

        imageButtonSettings = root.findViewById(R.id.imageButtonSettings);
        imageButtonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

        logout= root.findViewById(R.id.logout);

        offerAdapter = new DashboardFragment.OfferAdapter();
        offerRecyclerView.setAdapter(offerAdapter);
        offerRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Initialize EditText
        searchEditText = root.findViewById(R.id.search);

        // Initialize OfferAdapter
        offerAdapter = new DashboardFragment.OfferAdapter();
        offerRecyclerView.setAdapter(offerAdapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    dbHelper.searchOffers(s.toString().trim(), selectedCategory.toString(), new DatabaseHelper.OnOffersRetrievedListener() {
                        @Override
                        public void onOffersRetrieved(List<Offer> offers) {
                            offerAdapter.setOffers(offers);
                        }
                    });
                } else {
                    loadAllOffers(selectedCategory); // Call to show all offers when search is cleared
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        selectedCategory = categories[0];
        loadAllOffers(selectedCategory);
        return root;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadAllOffers(String selectedCategory) {

        if(selectedCategory != null && !selectedCategory.equals("Select Category")) {
            dbHelper.getOffersByCategory(selectedCategory, new DatabaseHelper.OnOffersRetrievedListener() {
                @Override
                public void onOffersRetrieved(List<Offer> offers) {
                    offerAdapter.setOffers(offers);
                }
            });
        } else {
            dbHelper.getAllOffers(new DatabaseHelper.OnOffersRetrievedListener() {
                @Override
                public void onOffersRetrieved(List<Offer> offers) {
                    offerAdapter.setOffers(offers);
                }
            });
        }
    }


    private void signOutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), Login.class);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish(); // Correct way to finish the Activity from Fragment
        }
    }

    private class OfferAdapter extends RecyclerView.Adapter<DashboardFragment.OfferAdapter.OfferViewHolder> {
        private List<Offer> offers;

        public void setOffers(List<Offer> offers) {
            this.offers = offers;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public DashboardFragment.OfferAdapter.OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.offer_item, parent, false);
            return new DashboardFragment.OfferAdapter.OfferViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DashboardFragment.OfferAdapter.OfferViewHolder holder, int position) {
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
                    Intent intent = new Intent(getActivity(), EditOfferActivity.class);
                    intent.putExtra("offer", offer);
                    startActivity(intent);
                });
            }
        }
    }
}