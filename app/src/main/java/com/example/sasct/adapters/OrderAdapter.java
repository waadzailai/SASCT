package com.example.sasct.adapters;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.sasct.R;
import com.example.sasct.TrackingActivity;
import com.example.sasct.model.Order;

import java.util.List;
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orders;
    private LayoutInflater inflater;


    public OrderAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders = orders;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_card, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.numberOrder.setText("Order #" + order.getId());
        holder.dateOrder.setText("Expected arrival time: " + order.getDate());
        holder.statusOrder.setText(order.getStatus());

        // Optionally set up any click listeners or additional data
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TrackingActivity.class);
            intent.putExtra("orderId", order.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView numberOrder, dateOrder, statusOrder;

        public OrderViewHolder(View itemView) {
            super(itemView);
            numberOrder = itemView.findViewById(R.id.numberOrder);
            dateOrder = itemView.findViewById(R.id.dateOrder);
            statusOrder = itemView.findViewById(R.id.statusOrder);
        }
    }
}