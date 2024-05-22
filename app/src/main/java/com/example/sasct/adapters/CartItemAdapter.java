package com.example.sasct.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sasct.R;
import com.example.sasct.model.CartItem;

import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder> {
    private List<CartItem> cartItems;
    private Context context;
    private OnCartItemActionListener actionListener;

    public CartItemAdapter(Context context, List<CartItem> cartItems, OnCartItemActionListener actionListener) {
        this.context = context;
        this.cartItems = cartItems;
        this.actionListener = actionListener;
    }

    @Override
    public CartItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
        return new CartItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CartItemViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);

        holder.productNameTextView.setText(cartItem.getName());
        holder.priceTextView.setText("$ " + cartItem.getPrice());
        holder.counterTextView.setText(String.valueOf(cartItem.getQuantity()));

        // Loading image using Glide
        Glide.with(context).load(cartItem.getImageUrl()).into(holder.imageView);

        holder.addButton.setOnClickListener(v -> actionListener.onIncreaseQuantity(cartItem));
        // Check quantity to determine button visibility
        if (cartItem.getQuantity() > 1) {
            holder.deleteButton.setVisibility(View.GONE);
            holder.decreaseButton.setVisibility(View.VISIBLE);

            holder.decreaseButton.setOnClickListener(v -> actionListener.onDecreaseQuantity(cartItem));
        } else {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.decreaseButton.setVisibility(View.GONE);

            holder.deleteButton.setOnClickListener(v -> actionListener.onDeleteItem(cartItem));
        }

    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public class CartItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView productNameTextView;
        TextView priceTextView;
        TextView counterTextView;
        ImageButton deleteButton;
        ImageButton addButton;
        ImageButton decreaseButton;

        public CartItemViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            counterTextView = itemView.findViewById(R.id.counterTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            addButton = itemView.findViewById(R.id.addButton);
            decreaseButton = itemView.findViewById(R.id.decreaseButton);
        }
    }

    public interface OnCartItemActionListener {
        void onDeleteItem(CartItem cartItem);
        void onDecreaseQuantity(CartItem cartItem);
        void onIncreaseQuantity(CartItem cartItem);
    }
}