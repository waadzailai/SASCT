package com.example.sasct.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.sasct.AddressActivity;
import com.example.sasct.databinding.FragmentCartBinding;
import com.example.sasct.db.DatabaseHelper;
import com.example.sasct.model.CartItem;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.sasct.R;
import com.example.sasct.adapters.CartItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {
    private FragmentCartBinding binding;
    private View root;
    private DatabaseHelper databaseHelper;
    private RecyclerView offersRecyclerView;
    private CartItemAdapter adapter;
    private List<CartItem> cartItems = new ArrayList<>();
    private String userId;
    private Button btnCheckout;
    private Button checkoutButton;
    private Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        databaseHelper = new DatabaseHelper(getContext());
        userId = databaseHelper.getCurrentUserId();
        btnCheckout = root.findViewById(R.id.btnCheckout);

        // Initialize toolbar
        toolbar = root.findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.title_fragment_cart);

        ImageView backButton = toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getActivity().onBackPressed());

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAddress(v);
            }
        });

        offersRecyclerView = binding.offersRecyclerView; // RecyclerView from XML
        offersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new CartItemAdapter(getContext(), cartItems, new CartItemAdapter.OnCartItemActionListener() {
            @Override
            public void onDeleteItem(CartItem cartItem) {
                removeCartItem(cartItem.getOffertId());
                viewOrHideCheckout();
            }

            @Override
            public void onDecreaseQuantity(CartItem cartItem) {
                updateCartItemQuantity(cartItem.getOffertId(), cartItem.getQuantity() - 1);
                viewOrHideCheckout();
            }

            @Override
            public void onIncreaseQuantity(CartItem cartItem) {
                updateCartItemQuantity(cartItem.getOffertId(), cartItem.getQuantity() + 1);
                viewOrHideCheckout();
            }


        });

        offersRecyclerView.setAdapter(adapter);

        fetchCartItems(userId);

        return root;
    }

    private void fetchCartItems(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).collection("cart")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    cartItems.clear(); // Clear previous items

                    for (DocumentSnapshot document : querySnapshot) {
                        CartItem cartItem = document.toObject(CartItem.class);
                        cartItems.add(cartItem);
                    }

                    viewOrHideCheckout();

                    adapter.notifyDataSetChanged(); // Notify adapter of data change
                })
                .addOnFailureListener(e -> {
                    // Handle failure appropriately
                    viewOrHideCheckout();
                    Toast.makeText(getContext(), "Your cart is empty!", Toast.LENGTH_SHORT).show();
                });
    }

    private void removeCartItem(String offerId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).collection("cart")
                .document(offerId).delete()
                .addOnSuccessListener(aVoid -> {
                    fetchCartItems(userId); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    // Handle failure appropriately
                    viewOrHideCheckout();
                });
    }

    private void updateCartItemQuantity(String offerId, int newQuantity) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).collection("cart")
                .document(offerId).update("quantity", newQuantity)
                .addOnSuccessListener(aVoid -> {
                    fetchCartItems(userId); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    // Handle failure appropriately
                    viewOrHideCheckout();
                });
    }

    private void viewOrHideCheckout(){
        if(cartItems.isEmpty()){
            btnCheckout.setVisibility(View.INVISIBLE);
            Toast.makeText(getContext(), "Your cart is empty!", Toast.LENGTH_SHORT).show();
        } else {
            btnCheckout.setVisibility(View.VISIBLE);
        }
    }

    public void goToAddress(View v){
        Intent intent = new Intent(getActivity(), AddressActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
