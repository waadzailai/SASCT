package com.example.sasct;

import static android.content.ContentValues.TAG;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import com.example.sasct.db.DatabaseHelper;
import com.example.sasct.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.sasct.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseHelper dbHelper;
    private ActivityMainBinding binding;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        dbHelper = new DatabaseHelper(this);
        userId = dbHelper.getCurrentUserId();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);


        // Fetch user data from Fire-store using DBHelper
        dbHelper.getUserData(userId, new DatabaseHelper.OnUserRetrievedListener() {
            @Override
            public void onUserRetrieved(User user) {
                runOnUiThread(() -> {
                    String userRole = user.getRole();
                    Menu menu = navView.getMenu();
                    if ("buyer".equals(userRole)) {
                        menu.findItem(R.id.navigation_create_offer).setVisible(false);
                        menu.findItem(R.id.navigation_cart).setVisible(true);
                    } else {
                        menu.findItem(R.id.navigation_create_offer).setVisible(true);
                        menu.findItem(R.id.navigation_cart).setVisible(false);
                    }
                });
            }

            @Override
            public void onUserRetrievalFailed(String errorMessage) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Failed to retrieve user data: " + errorMessage);
                    // Optional: Display a Toast or a Snackbar with the error message
                });
            }
        });

    }


}


