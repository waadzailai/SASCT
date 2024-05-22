package com.example.sasct;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ViewOfferSupplierActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_view_offer_supplier);


        Spinner spinnerShipment = findViewById(R.id.spinner_shipment);


        String[] shipmentMethods = {"Select Category", "Land", "Air", "Sea"};


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, shipmentMethods);


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        spinnerShipment.setAdapter(adapter);

        spinnerShipment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                String selectedItem = (String) parentView.getItemAtPosition(position);
                if (!selectedItem.equals("Select Category")) {

                    Toast.makeText(ViewOfferSupplierActivity.this, "Selected item: " + selectedItem, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
    }
}
