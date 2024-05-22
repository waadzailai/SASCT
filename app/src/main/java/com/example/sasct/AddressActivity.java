package com.example.sasct;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sasct.db.DatabaseHelper;
import com.example.sasct.model.CartItem;
import com.example.sasct.model.CreditCard;
import com.example.sasct.model.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sasct.adapters.AddressAdapter;
import com.example.sasct.model.Address;

import java.util.ArrayList;
import java.util.List;

public class AddressActivity extends AppCompatActivity {
    private RecyclerView addressesRecyclerView;
    private AddressAdapter adapter;
    private List<Address> addresses = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private FirebaseFirestore db;
    private Button proceedToPayment;
    private String userId, activeAddressId = null;
    private TextView totalAmountTextView;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        getSupportActionBar().hide();

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.title_activity_address);

        ImageView backButton = toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> this.onBackPressed());

        databaseHelper = new DatabaseHelper(this);
        db = FirebaseFirestore.getInstance();
        userId = databaseHelper.getCurrentUserId();

        addressesRecyclerView = findViewById(R.id.offersRecyclerView);
        addressesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        totalAmountTextView = findViewById(R.id.totalAmount);

        proceedToPayment = findViewById(R.id.proceedToPayment);
        proceedToPayment.setOnClickListener(v -> {
            if (activeAddressId != null) {
                // Retrieve the address object using the activeAddressId or directly use it
                Address selectedAddress = getAddressById(activeAddressId);
                proceedToCheckout(selectedAddress); // Navigate to the payment step
            } else {
                // Display a Toast message
                Toast.makeText(this, "Please select an address before proceeding", Toast.LENGTH_LONG).show();
            }
        });


        adapter = new AddressAdapter(this, addresses, new AddressAdapter.OnAddressActionListener() {
            @Override
            public void onRemoveAddress(String addressId) {
                removeAddress(addressId);
            }

            @Override
            public void onEditAddress(Address address) {
                navigateToEditAddress(address);
            }

            @Override
            public void onChooseAddress(Address address) {
                activeAddressId = address.getId(); // Track the active address ID
//                proceedToCheckout(address);
            }
        }, activeAddressId);

        addressesRecyclerView.setAdapter(adapter);

        fetchAddresses(userId);
        fetchTotalPrice();
    }

    // Helper function to retrieve an Address object by its ID
    private Address getAddressById(String addressId) {
        for (Address address : addresses) {
            if (address.getId().equals(addressId)) {
                return address;
            }
        }
        return null; // Address not found
    }

    private void fetchAddresses(String userId) {
        db.collection("users").document(userId).collection("addresses")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    addresses.clear();

                    for (DocumentSnapshot document : querySnapshot) {
                        Address address = document.toObject(Address.class);
                        addresses.add(address);
                    }

                    adapter = new AddressAdapter(this, addresses, new AddressAdapter.OnAddressActionListener() {
                        @Override
                        public void onRemoveAddress(String addressId) {
                            removeAddress(addressId);
                        }

                        @Override
                        public void onEditAddress(Address address) {
                            navigateToEditAddress(address);
                        }

                        @Override
                        public void onChooseAddress(Address address) {
                            activeAddressId = address.getId(); // Track the selected address ID
//                            proceedToCheckout(address); // Directly navigate to checkout
                        }
                        }, activeAddressId);

                    addressesRecyclerView.setAdapter(adapter);

                    adapter.notifyDataSetChanged(); // Update RecyclerView
                })
                .addOnFailureListener(e -> {
                    // Handle failure appropriately
                });
    }

    private void removeAddress(String addressId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).collection("addresses")
                .document(addressId).delete()
                .addOnSuccessListener(aVoid -> fetchAddresses(userId))
                .addOnFailureListener(e -> {
                    // Handle failure appropriately
                });
    }

    private void proceedToCheckout(Address selectedAddress) {
        // Create an Intent to navigate to the PaymentActivity
        Intent intent = new Intent(this, PaymentActivity.class);

        // Pass the selected address as a Serializable extra
        intent.putExtra("selectedAddress", selectedAddress);

        // Start the PaymentActivity
        startActivityForResult(intent, 200);
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


    public void navigateToCreateAddress(View view) {
        Intent intent = new Intent(this, EditAddressActivity.class);
        startActivityForResult(intent, 100);
    }

    private void navigateToEditAddress(Address address) {
        Intent intent = new Intent(this, EditAddressActivity.class);

        if (address != null) {
            intent.putExtra("address", address); // Pass the address object for editing
        }

        startActivityForResult(intent, 100);
    }

    private void navigateToPayment(Address address) {
        Intent intent = new Intent(this, PaymentActivity.class); // Navigate to the PaymentActivity

        intent.putExtra("selectedAddress", address); // Pass the selected address for order creation

        startActivityForResult(intent, 200); // Use startActivityForResult to handle order creation flow
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) { // Check the request and result codes
            fetchAddresses(userId);
            fetchTotalPrice();
        }
    }
}