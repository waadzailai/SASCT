package com.example.sasct.db;

import android.util.Log;

import com.example.sasct.model.CartItem;
import com.example.sasct.model.Offer;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private FirebaseFirestore db;

    public CartManager() {
        db = FirebaseFirestore.getInstance();
    }

    public void addOfferToCart(String userId, Offer offer, OnCartActionListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the document representing this offer in the user's cart
        DocumentReference cartItemRef = db.collection("users").document(userId).collection("cart")
                .document(offer.getId());

        // Check if the item is already in the cart
        cartItemRef.get().addOnSuccessListener(document -> {
            if (document.exists()) {
                // Item already in cart, increase quantity
                CartItem existingItem = document.toObject(CartItem.class);
                int newQuantity = existingItem.getQuantity() + 1;

                cartItemRef.update("quantity", newQuantity)
                        .addOnSuccessListener(aVoid -> listener.onActionSuccess())
                        .addOnFailureListener(e -> listener.onActionFailure(e.getMessage()));
            } else {
                // Item not in cart, add it
                CartItem cartItem = new CartItem(
                        offer.getId(),
                        offer.getTitle(),
                        offer.getImageUrl(),
                        offer.getPrice(),
                        1 // Initial quantity
                );

                cartItemRef.set(cartItem)
                        .addOnSuccessListener(aVoid -> listener.onActionSuccess())
                        .addOnFailureListener(e -> listener.onActionFailure(e.getMessage()));
            }
        }).addOnFailureListener(e -> listener.onActionFailure(e.getMessage()));
    }

    public void updateCartItemQuantity(String userId, String offerId, int newQuantity, OnCartActionListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).collection("cart")
                .document(offerId).update("quantity", newQuantity)
                .addOnSuccessListener(aVoid -> listener.onActionSuccess())
                .addOnFailureListener(e -> listener.onActionFailure(e.getMessage()));
    }


    public void removeCartItem(String userId, String offerId, OnCartActionListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).collection("cart")
                .document(offerId).delete()
                .addOnSuccessListener(aVoid -> listener.onActionSuccess())
                .addOnFailureListener(e -> listener.onActionFailure(e.getMessage()));
    }

    public void fetchCartItems(String userId, OnFetchCartItemsListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).collection("cart")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<CartItem> cartItems = new ArrayList<>();

                    for (DocumentSnapshot document : querySnapshot) {
                        CartItem cartItem = document.toObject(CartItem.class);
                        cartItems.add(cartItem);
                    }

                    listener.onFetchSuccess(cartItems);
                })
                .addOnFailureListener(e -> listener.onFetchFailure(e.getMessage()));
    }

    public interface OnCartActionListener {
        void onActionSuccess();
        void onActionFailure(String errorMessage);
    }

    public interface OnFetchCartItemsListener {
        void onFetchSuccess(List<CartItem> cartItems);
        void onFetchFailure(String errorMessage);
    }
}
