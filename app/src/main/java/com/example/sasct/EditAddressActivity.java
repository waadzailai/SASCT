package com.example.sasct;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.sasct.db.DatabaseHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.sasct.model.Address;

public class EditAddressActivity extends AppCompatActivity {
    private EditText streetEditText, cityEditText, stateEditText, postalCodeEditText, countryEditText,additionalInfoEditText;
    private Button saveButton;
    private String addressId, userId;
    private DatabaseHelper databaseHelper;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_address);
        getSupportActionBar().hide();

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.title_activity_add_address);

        ImageView backButton = toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> this.onBackPressed());

        streetEditText = findViewById(R.id.streetEditText);
        cityEditText = findViewById(R.id.cityEditText);
        stateEditText = findViewById(R.id.stateEditText);
        postalCodeEditText = findViewById(R.id.postalCodeEditText);
        countryEditText = findViewById(R.id.countryEditText);
        additionalInfoEditText = findViewById(R.id.additionalInfoEditText);

        saveButton = findViewById(R.id.saveButton);

        databaseHelper = new DatabaseHelper(this);
        userId = databaseHelper.getCurrentUserId();

        // Check if we're editing an existing address
        Address address = (Address) getIntent().getSerializableExtra("address");

        if (address != null) {
            populateAddressFields(address);
            addressId = address.getId(); // Track the ID for updates
        }

        saveButton.setOnClickListener(view -> saveOrUpdateAddress());
    }

    private void populateAddressFields(Address address) {
        streetEditText.setText(address.getStreet());
        cityEditText.setText(address.getCity());
        stateEditText.setText(address.getState());
        postalCodeEditText.setText(address.getPostalCode());
        countryEditText.setText(address.getCountry());
        additionalInfoEditText.setText(address.getAdditionalInfo());
    }

    private void saveOrUpdateAddress() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Address newAddress = new Address(
                null, // Temporarily null, but we'll update it with the Firestore ID
                streetEditText.getText().toString(),
                cityEditText.getText().toString(),
                stateEditText.getText().toString(),
                postalCodeEditText.getText().toString(),
                countryEditText.getText().toString(),
                additionalInfoEditText.getText().toString()
        );


        if (addressId == null) {
            db.collection("users").document(userId).collection("addresses")
                    .add(newAddress)
                    .addOnSuccessListener(documentReference -> {
                        newAddress.setId(documentReference.getId()); // Set the document ID as the address ID

                        documentReference.set(newAddress) // Update the document with the correct ID
                                .addOnSuccessListener(aVoid -> {
                                    setResult(RESULT_OK); // Notify the parent activity of success
                                    finish(); // Finish the current activity
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure appropriately
                                });
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure appropriately
                    });
        } else {
            // Update existing address directly
            db.collection("users").document(userId).collection("addresses")
                    .document(addressId).set(newAddress)
                    .addOnSuccessListener(aVoid -> {
                        setResult(RESULT_OK); // Notify the parent activity of success
                        finish(); // Finish the current activity
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure appropriately
                    });
        }
    }

}