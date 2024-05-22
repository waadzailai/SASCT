package com.example.sasct;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ShippingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_shipping);
    }
}