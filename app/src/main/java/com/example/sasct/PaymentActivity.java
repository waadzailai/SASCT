package com.example.sasct;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.sasct.adapters.AddressAdapter;
import com.example.sasct.db.DatabaseHelper;
import com.example.sasct.model.Address;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.sasct.adapters.CardAdapter;
import com.example.sasct.model.CreditCard;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.example.sasct.adapters.CardAdapter;
import com.example.sasct.model.CreditCard;
import com.example.sasct.model.CreditCard;
import com.example.sasct.model.CartItem;
import com.example.sasct.model.Order;


public class PaymentActivity extends AppCompatActivity {
    private RecyclerView cardsRecyclerView;
    private DatabaseHelper databaseHelper;
    private CardAdapter adapter;
    private List<CreditCard> cards = new ArrayList<>();
    private Address selectedAddress; // Track the selected address
    private CreditCard selectedCard; // Track the selected address
    private String activeCardId, userId; // Track the selected card ID
    private TextView totalAmountTextView;
    private Button orderSubmitButton;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        getSupportActionBar().hide();

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.title_activity_payment);

        ImageView backButton = toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> this.onBackPressed());

        databaseHelper = new DatabaseHelper(this);
        userId = databaseHelper.getCurrentUserId();

        cardsRecyclerView = findViewById(R.id.cardsRecyclerView);
        cardsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        totalAmountTextView = findViewById(R.id.totalAmount);
        selectedAddress = (Address) getIntent().getSerializableExtra("selectedAddress"); // Fetch the selected address
        // Set up the adapter and fetch initial data

        adapter = new CardAdapter(this, cards, new CardAdapter.OnCardActionListener() {
            @Override
            public void onRemoveCard(String cardId) {
                removeCard(cardId);
            }

            @Override
            public void onEditCard(CreditCard card) {
                navigateToEditCard(card);
            }

            @Override
            public void onChooseCard(CreditCard card) {
                activeCardId = card.getId();
            }
        }, activeCardId);

        cardsRecyclerView.setAdapter(adapter);

        orderSubmitButton = findViewById(R.id.orderSubmit);
        orderSubmitButton.setOnClickListener(v -> {
            if (activeCardId != null) {
                // Retrieve the address object using the activeAddressId or directly use it
                CreditCard selectedCard = getCardById(activeCardId);
                createOrder(selectedAddress, selectedCard);
            } else {
                // Display a Toast message
                Toast.makeText(this, "Please select a card before proceeding", Toast.LENGTH_LONG).show();
            }
        });

        fetchCards(userId);
        fetchTotalPrice();
    }

    private void fetchCards(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).collection("cards")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    cards.clear();

                    for (DocumentSnapshot document : querySnapshot) {
                        CreditCard card = document.toObject(CreditCard.class);
                        cards.add(card);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle failure appropriately
                });
    }

    private CreditCard getCardById(String cardId) {
        for (CreditCard card : cards) {
            if (card.getId().equals(cardId)) {
                return card;
            }
        }
        return null; // Card not found
    }

    private void createOrder(Address address, CreditCard card) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).collection("cart")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<CartItem> cartItems = new ArrayList<>();
                double totalPrice = 0;

                for (DocumentSnapshot document : querySnapshot) {
                    CartItem cartItem = document.toObject(CartItem.class);
                    cartItems.add(cartItem);
                    totalPrice += cartItem.getPrice() * cartItem.getQuantity();
                }

                Order order = new Order(cartItems, address, card, totalPrice);

                db.collection("users").document(userId).collection("orders")
                    .add(order)
                    .addOnSuccessListener(documentReference  -> {
                        String documentId = documentReference.getId();
                        order.setId(documentId);  // Set the document ID to the order object

                        // Optionally, you might want to update the Firestore document with the new ID
                        documentReference.update("id", documentId)
                            .addOnSuccessListener(aVoid -> {
                                // Document ID is now updated in Firestore, you can clear the cart
                                clearCartItems();
                                navigateToThanksActivity();
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure in updating document ID
                                Log.e("UpdateFail", "Failed to update document with ID", e);
                            });
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure appropriately
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle failure appropriately
                });
    }

    private void clearCartItems() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).collection("cart")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                for (DocumentSnapshot document : querySnapshot) {
                    db.collection("users").document(userId).collection("cart")
                        .document(document.getId()).delete()
                        .addOnFailureListener(e -> {
                            // Handle failure appropriately
                        });
                }
            })
            .addOnFailureListener(e -> {
                // Handle failure appropriately
            });
    }

    private void removeCard(String cardId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).collection("cards")
                .document(cardId).delete()
                .addOnSuccessListener(aVoid -> fetchCards(userId)) // Refresh cards
                .addOnFailureListener(e -> {
                    // Handle failure appropriately
                });
    }

    private void fetchTotalPrice() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).collection("cart")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    double totalPrice = 0;

                    for (DocumentSnapshot document : querySnapshot) {
                        CartItem cartItem = document.toObject(CartItem.class);
                        totalPrice += cartItem.getPrice() * cartItem.getQuantity(); // Sum up the total
                    }

                    totalAmountTextView.setText("$ " + totalPrice); // Display the total price
                })
                .addOnFailureListener(e -> {
                    // Handle failure appropriately
                });
    }

    public void navigateToCreateCreditCard(View view) {
        Intent intent = new Intent(this, EditCardActivity.class);
        startActivityForResult(intent, 100);
    }

    private void navigateToEditCard(CreditCard card) {
        Intent intent = new Intent(this, EditCardActivity.class);

        if (card != null) {
            intent.putExtra("card", card); // Pass card for editing
        }

        startActivityForResult(intent, 150); // Use startActivityForResult for updates
    }

    private void navigateToThanksActivity() {
        Intent intent = new Intent(this, ThanksActivity.class);
        startActivity(intent); // Navigate to the thanks page
        finish(); // Complete the current activity
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == 100 || requestCode == 150) && resultCode == RESULT_OK) {
            // Refresh cards and UI after editing or adding a card
            fetchCards(userId);
            fetchTotalPrice();
        }
    }

}