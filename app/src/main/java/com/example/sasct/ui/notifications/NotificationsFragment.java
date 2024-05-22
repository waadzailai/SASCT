package com.example.sasct.ui.notifications;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sasct.OfferResponseActivity;
import com.example.sasct.OfferResponseBuyerActivity;
import com.example.sasct.R;
import com.example.sasct.databinding.FragmentNotificationsBinding;
import com.example.sasct.db.DatabaseHelper;
import com.example.sasct.model.NegotiatedOffer;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private LinearLayout parentLayout;
    private Toolbar toolbar;
    private FrameLayout negotiationHolder;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize toolbar
        toolbar = root.findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.title_fragment_negotiations);

        ImageView backButton = toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getActivity().onBackPressed());

        parentLayout = binding.getRoot().findViewById(R.id.parentLayout);
        negotiationHolder = binding.getRoot().findViewById(R.id.negotiationHolder);
        // Check user role and fetch offers accordingly
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        String currentUserId = databaseHelper.getCurrentUserId();
        databaseHelper.getCurrentUserRole(new DatabaseHelper.OnUserRoleListener() {
            @Override
            public void onSuccess(String role) {
                if (role.equals("buyer")) {
                    fetchAndDisplayOffers(currentUserId, "buyer");
                } else if (role.equals("supplier")) {
                    fetchAndDisplayOffers(currentUserId, "supplier");
                } else {
                    Toast.makeText(getContext(), "Error: Unknown user role", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void fetchAndDisplayOffers(String userId, String role) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("negotiated_offers")
                .whereEqualTo(role.equals("buyer") ? "buyerId" : "supplierId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        NegotiatedOffer offer = doc.toObject(NegotiatedOffer.class);
                        addOfferToView(offer, role);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching offers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addOfferToView(NegotiatedOffer offer, String role) {
        View cardView = LayoutInflater.from(getContext()).inflate(R.layout.offer_card_layout, null);

        // Populate the card view
        TextView numberOrder = cardView.findViewById(R.id.numberOrder);
        TextView dateOrder = cardView.findViewById(R.id.dateOrder);
        TextView statusOrder = cardView.findViewById(R.id.statusOrder);
        negotiationHolder = cardView.findViewById(R.id.negotiationHolder); // Ensure correct ID

        if (negotiationHolder == null) {
            System.out.println("Error: negotiationHolder is null");
            return; // Exit early if not found
        }

        numberOrder.setText("Negotiated Offer ID: " + offer.getOfferId());
        dateOrder.setText("Price: " + offer.getNegotiatedPrice() + ", Quantity: " + offer.getNegotiatedQuantity());
        statusOrder.setText("Status: " + offer.getStatus());

        // Set background tint based on status
        String status = offer.getStatus();
        int colorResId = R.color.pendingColor; // Default color for pending

        if (status.equalsIgnoreCase("Accepted")) {
            colorResId = R.color.acceptedColor;
        } else if (status.equalsIgnoreCase("Rejected")) {
            colorResId = R.color.rejectedColor;
        } else if (status.equalsIgnoreCase("Edited")) {
            colorResId = R.color.editedColor;
        }

        // Set the background tint
        negotiationHolder.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), colorResId));

        // Click listener to open OfferResponseActivity
        cardView.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), role.equals("buyer") ? OfferResponseBuyerActivity.class : OfferResponseActivity.class);

            intent.putExtra("offer", offer);

            // Start the OfferResponseActivity
            startActivityForResult(intent, 200);
        });

        // Add the card view to the parent layout
        parentLayout.addView(cardView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == RESULT_OK) {
            // Clear existing views
            parentLayout.removeAllViews();

            // Refetch and display offers
            DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
            String currentUserId = databaseHelper.getCurrentUserId();
            databaseHelper.getCurrentUserRole(new DatabaseHelper.OnUserRoleListener() {
                @Override
                public void onSuccess(String role) {
                    if (role.equals("buyer")) {
                        fetchAndDisplayOffers(currentUserId, "buyer");
                    } else if (role.equals("supplier")) {
                        fetchAndDisplayOffers(currentUserId, "supplier");
                    } else {
                        Toast.makeText(getContext(), "Error: Unknown user role", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
