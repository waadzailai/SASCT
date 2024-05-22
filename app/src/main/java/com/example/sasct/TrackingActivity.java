package com.example.sasct;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final String TAG = "TrackingActivity";
    private boolean isText1Clicked = false;
    private boolean isText2Clicked = false;
    private boolean isText3Clicked = false;
    private int originalTextColor;
    private Toolbar toolbar;
    // Firebase
    private DatabaseReference sensorsRef;
    private MapView mapView;
    private GoogleMap googleMap;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        getSupportActionBar().hide();

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.title_activity_tracking);

        ImageView backButton = toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> this.onBackPressed());

        final TextView approximateTime = findViewById(R.id.approximateTime);
        final TextView humidityPlace = findViewById(R.id.humidity);
        final TextView temperature = findViewById(R.id.temperature);

        originalTextColor = approximateTime.getCurrentTextColor();

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        sensorsRef = database.getReference("Sensor");

        // Initialize MapView
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        // Read from the database
        sensorsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get sensor data from Firebase and update TextViews
                String humidity = dataSnapshot.child("hemduity").getValue(String.class);
                String temp = dataSnapshot.child("temperature").getValue(String.class);
                Double latitude = dataSnapshot.child("gps/latitude").getValue(Double.class);
                Double longitude = dataSnapshot.child("gps/longitude").getValue(Double.class);

                humidityPlace.setText(humidity != null ? String.valueOf(humidity) + " %" : "N/A");
                temperature.setText(temp != null ? String.valueOf(temp) + " C" : "N/A");

                Log.d(TAG, "Latitude: " + latitude + ", Longitude: " + longitude);
                Toast.makeText(TrackingActivity.this, "Lat: " + latitude + ", Lng: " + longitude, Toast.LENGTH_LONG).show();

                // Update the map with the GPS location
                if (latitude != null && longitude != null) {
                    LatLng location = new LatLng(latitude, longitude);
                    if (googleMap != null) {
                        googleMap.clear();
                        googleMap.addMarker(new MarkerOptions().position(location).title("Sensor Location"));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                    } else {
                        Log.e(TAG, "Latitude or Longitude is null");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Log.e(TAG, "Database Error: " + databaseError.getMessage());
            }
        });

        approximateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (approximateTime.getText().toString().equals("12 hours")) {
                    approximateTime.setText("Approximate time remaining to arrive");
                    approximateTime.setTextColor(originalTextColor);
                    approximateTime.setText("12 hours");
                    approximateTime.setTextColor(Color.parseColor("#dc8c44"));
                }
            }
        });

        humidityPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTextViewProperties(humidityPlace, 2);
            }
        });

        temperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTextViewProperties(temperature, 3);
            }
        });
    }

    private void toggleTextViewProperties(final TextView textView, final int textViewId) {
        // Check the ID of the clicked TextView
        switch (textViewId) {
            case 2: // For humidityPlace
                isText2Clicked = !isText2Clicked;
                if (isText2Clicked) {
                    textView.setText("Loading..."); // Display loading message
                    textView.setTextColor(Color.parseColor("#dc8c44"));
                    // Get data from Firebase and update TextView
                    sensorsRef.child("hemduity").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String humidity = dataSnapshot.getValue(String.class);
                            textView.setText(humidity != null ? String.valueOf(humidity) + " %" : "N/A");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle database error
                            Log.e(TAG, "Database Error: " + databaseError.getMessage());
                        }
                    });
                } else {
                    textView.setText("Humidity of the place");
                    textView.setTextColor(originalTextColor);
                }
                break;
            case 3: // For temperature
                isText3Clicked = !isText3Clicked;
                if (isText3Clicked) {
                    textView.setText("Loading..."); // Display loading message
                    textView.setTextColor(Color.parseColor("#dc8c44"));
                    // Get data from Firebase and update TextView
                    sensorsRef.child("temperature").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String temperature = dataSnapshot.getValue(String.class);
                            textView.setText(temperature != null ? String.valueOf(temperature) + " C" : "N/A");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle database error
                            Log.e(TAG, "Database Error: " + databaseError.getMessage());
                        }
                    });
                } else {
                    textView.setText("Temperature");
                    textView.setTextColor(originalTextColor);
                }
                break;
        }
    }
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        Log.d(TAG, "GoogleMap is ready");
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }
}
