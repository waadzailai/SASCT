package com.example.sasct;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import com.example.sasct.model.Offer;
import com.example.sasct.db.DatabaseHelper;


public class CreateOfferActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText etTitle, etDescription, etPrice, etQuantity;
    private Spinner spinnerCategory, spinnerShipment;
    private ImageView btnSelectImage;
    private String selected_category, selected_shipment;
    private Button btnSubmit, btnCancel;
    private ImageView imgPreview;
    private Uri imageUri;
    private boolean isImageSelected = false;
    private DatabaseHelper databaseHelper;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_offer);
        getSupportActionBar().hide();

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.title_activity_create_offer);

        ImageView backButton = toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> this.onBackPressed());

        // Initialize UI components
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etQuantity = findViewById(R.id.etQuantity);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerShipment = findViewById(R.id.spinner_shipment);

        // Initialize DatabaseHelper instance
        databaseHelper = new DatabaseHelper(this);

        // Define arrays for categories and shipment methods
        String[] categories = getResources().getStringArray(R.array.categories_array);
        String[] shipmentMethods = getResources().getStringArray(R.array.shipment_array);

        // Create ArrayAdapter for categories
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setSelection(0);
        spinnerCategory.setAdapter(categoryAdapter);

        // Create ArrayAdapter for shipment methods
        ArrayAdapter<String> shipmentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, shipmentMethods);
        shipmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShipment.setSelection(0);
        spinnerShipment.setAdapter(shipmentAdapter);

        // Handle category selection
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selected_category = (String) parentView.getItemAtPosition(position);
                if (!selected_category.equals("Select Category")) {
                    Toast.makeText(getApplicationContext(), "Selected category: " + selected_category, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing if nothing is selected
            }
        });

        // Handle shipment method selection
        spinnerShipment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selected_shipment = (String) parentView.getItemAtPosition(position);
                if (!selected_shipment.isEmpty()) {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing if nothing is selected
            }
        });

        // Handle image selection button click
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        btnCancel.setOnClickListener(v -> this.onBackPressed());

        // Handle submit button click
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve user input
                String title = etTitle.getText().toString();
                String description = etDescription.getText().toString();
                double price = Double.parseDouble(etPrice.getText().toString());
                int quantity = Integer.parseInt(etQuantity.getText().toString());

                // Validate input fields
                if (title.isEmpty() || description.isEmpty() || selected_category.equals("Select Category") || selected_shipment.equals("Select Shipment") || price == 0 || quantity == 0 || imageUri == null) {
                    Toast.makeText(CreateOfferActivity.this, "Please fill in all fields and select an image", Toast.LENGTH_SHORT).show();
                } else {
                    // Save the offer
                    databaseHelper.createOffer(null, title, description, selected_category, selected_shipment, price, quantity, imageUri, new DatabaseHelper.OnOfferCreatedListener() {
                        @Override
                        public void onOfferCreated(Offer offer) {
                            // Offer added successfully
                            Toast.makeText(CreateOfferActivity.this, "Offer created successfully", Toast.LENGTH_SHORT).show();
                            intentToMain();
                        }

                        @Override
                        public void onOfferCreationFailed(String errorMessage) {
                            // Handle failure
                            Toast.makeText(CreateOfferActivity.this, "Failed to create offer: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
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

    private void intentToMain() {
        startActivity(new Intent(CreateOfferActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Assign the selected image URI to the class-level variable
            imageUri = data.getData();
            // Display a preview of the selected image
            btnSelectImage.setImageURI(imageUri);
            isImageSelected = true;
        }
    }
}
