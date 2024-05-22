package com.example.sasct;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sasct.db.DatabaseHelper;
import com.example.sasct.model.Offer;
import com.squareup.picasso.Picasso;

public class EditOfferActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText etTitle, etDescription, etPrice, etQuantity;
    private ImageView imageView;
    private Uri imageUri;
    private Spinner spinnerCategory, spinnerShipment;
    private String selectedCategory, selectedShipment, imageUrl, userId;
    private Button btnEdit, btnDelete, btnCancel;
    private Offer offer;
    private DatabaseHelper databaseHelper;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_edit_offer);

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.title_activity_edit_offer);

        ImageView backButton = toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> this.onBackPressed());

        // Initialize UI components
        imageView = findViewById(R.id.btnSelectImage);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etQuantity = findViewById(R.id.etQuantity);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> this.onBackPressed());

        // Initialize DatabaseHelper instance
        databaseHelper = new DatabaseHelper(this);

        // Define the Spinners
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerShipment = findViewById(R.id.spinner_shipment);

        // Define arrays for categories and shipment methods
        String[] categories = getResources().getStringArray(R.array.categories_array);
        String[] shipmentMethods = getResources().getStringArray(R.array.shipment_array);

        // Create ArrayAdapter for categories
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Create ArrayAdapter for shipment methods
        ArrayAdapter<String> shipmentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, shipmentMethods);
        shipmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set ArrayAdapter to Spinners
        spinnerCategory.setAdapter(categoryAdapter);
        spinnerShipment.setAdapter(shipmentAdapter);

        // Handle category selection
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedCategory = (String) parentView.getItemAtPosition(position);
                if (!selectedCategory.equals("Select Category")) {
                    Toast.makeText(getApplicationContext(), "Selected category: " + selectedCategory, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing if nothing is selected
            }
        });

        // Handle shipment selection
        spinnerShipment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedShipment = (String) parentView.getItemAtPosition(position);
                if (!selectedShipment.equals("Select Category")) {
                    Toast.makeText(getApplicationContext(), "Selected shipment method: " + selectedShipment, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing if nothing is selected
            }
        });

        // Retrieve the Offer object from the intent
        offer = (Offer) getIntent().getSerializableExtra("offer");

        // Pre-fill the EditText fields with the existing offer attributes
        if (offer != null) {
            etTitle.setText(offer.getTitle());
            etDescription.setText(offer.getDescription());
            etPrice.setText(String.valueOf(offer.getPrice()));
            etQuantity.setText(String.valueOf(offer.getQuantity()));

            imageUrl = offer.getImageUrl();
            if(imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get()
                        .load(imageUrl)
                        .error(R.drawable.baseline_image_24) // Add an error drawable in your drawable resources
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.baseline_image_24); // Default image if URL is invalid
            }

            // Change button text to "Update" when editing an existing offer
            btnEdit.setText("Update");
        } else {
            // Change button text to "Create" when creating a new offer
            btnEdit.setText("Create");
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        // Handle update button click
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the offer with the new attribute values
                updateOffer();
            }
        });

        // Handle delete button click
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate input fields
                if (validateFields()) {
                    // Update the offer with the new attribute values
                    deleteOffer();
                }
            }
        });

        // Handle cancel button click
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the activity
                finish();
            }
        });
    }

    // Method to open image picker
    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // Method to update the offer
    private void updateOffer() {
        // Update offer attributes with values from EditText fields
        offer.setTitle(etTitle.getText().toString());
        offer.setDescription(etDescription.getText().toString());
        offer.setPrice(Double.parseDouble(etPrice.getText().toString()));
        offer.setQuantity(Integer.parseInt(etQuantity.getText().toString()));
        offer.setCategory(selectedCategory);
        offer.setImageUrl(imageUrl);

        // Call DatabaseHelper method to update the offer in the database
        databaseHelper.updateOffer(offer, new DatabaseHelper.OnOfferUpdatedListener() {
            @Override
            public void onOfferUpdated() {
                // Offer updated successfully
                setResultAndFinish(Activity.RESULT_OK, "Offer updated successfully");
            }

            @Override
            public void onOfferUpdateFailed(String errorMessage) {
                // Failed to update offer
                setResultAndFinish(Activity.RESULT_CANCELED, errorMessage);
            }
        });
    }

    // Method to delete the offer
    private void deleteOffer() {
        // Call DatabaseHelper method to delete the offer from the database
        databaseHelper.deleteOffer(offer, new DatabaseHelper.OnOfferDeletedListener() {
            @Override
            public void onOfferDeleted() {
                // Offer deleted successfully
                setResultAndFinish(Activity.RESULT_OK, "Offer deleted successfully");
            }

            @Override
            public void onOfferDeletionFailed(String errorMessage) {
                // Failed to delete offer
                setResultAndFinish(Activity.RESULT_CANCELED, errorMessage);
            }
        });
    }

    // Method to validate input fields
    private boolean validateFields() {
        if (etTitle.getText().toString().isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return false;
        }
        // Similarly, add validation for other fields
        return true;
    }

    // Method to set result and finish the activity
    private void setResultAndFinish(int resultCode, String message) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("message", message);
        setResult(resultCode, resultIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Assign the selected image URI to the class-level variable
            imageUri = data.getData();
            // Display a preview of the selected image
            imageView.setImageURI(imageUri);
        }
    }
}
