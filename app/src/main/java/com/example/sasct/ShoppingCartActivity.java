package com.example.sasct;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sasct.db.DatabaseHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.sasct.model.CartItem;
import com.example.sasct.adapters.CartItemAdapter;

public class ShoppingCartActivity extends AppCompatActivity {
    private RecyclerView offersRecyclerView;
    private DatabaseHelper databaseHelper;
    private CartItemAdapter adapter;
    private List<CartItem> cartItems = new ArrayList<>();
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_shopping_cart);
        databaseHelper = new DatabaseHelper(this);
        userId = databaseHelper.getCurrentUserId();

        offersRecyclerView = findViewById(R.id.offersRecyclerView);
        offersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CartItemAdapter(this, cartItems, new CartItemAdapter.OnCartItemActionListener() {
            @Override
            public void onDeleteItem(CartItem cartItem) {
                removeCartItem(cartItem.getOffertId());
            }

            @Override
            public void onDecreaseQuantity(CartItem cartItem) {
                updateCartItemQuantity(cartItem.getOffertId(), cartItem.getQuantity() - 1);
            }

            @Override
            public void onIncreaseQuantity(CartItem cartItem) {
                updateCartItemQuantity(cartItem.getOffertId(), cartItem.getQuantity() + 1);
            }
        });

        offersRecyclerView.setAdapter(adapter);

        fetchCartItems(userId);
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

                    adapter.notifyDataSetChanged(); // Notify adapter of data change
                })
                .addOnFailureListener(e -> {
                    // Handle failure appropriately
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
                });
    }

}