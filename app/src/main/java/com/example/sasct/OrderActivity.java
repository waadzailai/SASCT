package com.example.sasct;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sasct.db.DatabaseHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.sasct.adapters.OrderAdapter;
import com.example.sasct.model.Order;

import java.util.ArrayList;
import java.util.List;
public class OrderActivity extends AppCompatActivity {
    private RecyclerView ordersRecyclerView;
    private OrderAdapter adapter;
    private List<Order> orders = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private FirebaseFirestore db;
    private String userId;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiy_order);
        getSupportActionBar().hide();

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.title_activity_order);

        ImageView backButton = toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> this.onBackPressed());

        databaseHelper = new DatabaseHelper(this);
        db = FirebaseFirestore.getInstance();
        userId = databaseHelper.getCurrentUserId();

        ordersRecyclerView = findViewById(R.id.offersRecyclerView);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new OrderAdapter(this, orders);
        ordersRecyclerView.setAdapter(adapter);

        fetchOrders(userId);
    }

    private void fetchOrders(String userId) {
        db.collection("users").document(userId).collection("orders")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    orders.clear();

                    for (DocumentSnapshot document : querySnapshot) {
                        Order order = document.toObject(Order.class);
                        orders.add(order);
                    }

                    adapter.notifyDataSetChanged(); // Refresh RecyclerView UI
                })
                .addOnFailureListener(e -> {
                    // Handle failure appropriately
                });
    }
}