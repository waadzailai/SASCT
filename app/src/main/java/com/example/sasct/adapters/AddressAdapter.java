package com.example.sasct.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.sasct.R;
import com.example.sasct.model.Address;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
    private List<Address> addresses;
    private Context context;
    private OnAddressActionListener actionListener;
    private String activeAddressId;

    public AddressAdapter(Context context, List<Address> addresses, OnAddressActionListener actionListener, String activeAddressId) {
        this.context = context;
        this.addresses = addresses;
        this.actionListener = actionListener;
        this.activeAddressId = activeAddressId;
    }

    @Override
    public AddressViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.address_card, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AddressViewHolder holder, int position) {
        Address address = addresses.get(position);

        holder.SubAddress.setText(address.getStreet() + ", " + address.getCity() + ", " +
                address.getState() + " " + address.getPostalCode() + ", " +
                address.getCountry());

        holder.RemoveAddress.setOnClickListener(v -> actionListener.onRemoveAddress(address.getId()));
        holder.EditAddress.setOnClickListener(v -> actionListener.onEditAddress(address));
        holder.itemView.setOnClickListener(v -> {
            activeAddressId = address.getId(); // Set as active address
            actionListener.onChooseAddress(address); // Trigger checkout or save active state
            notifyDataSetChanged(); // Refresh the entire list to update the UI
        });

        // Show or hide tick based on current active address
        if (activeAddressId != null && activeAddressId.equals(address.getId())) {
            holder.ChooseAddress.setVisibility(View.VISIBLE); // Show tick
        } else {
            holder.ChooseAddress.setVisibility(View.GONE); // Hide tick
        }
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    public class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView SubAddress;
        ImageView RemoveAddress;
        ImageView EditAddress;
        ImageView ChooseAddress;

        public AddressViewHolder(View itemView) {
            super(itemView);

            SubAddress = itemView.findViewById(R.id.SubAddress);
            RemoveAddress = itemView.findViewById(R.id.RemoveAddress);
            EditAddress = itemView.findViewById(R.id.EditAddress);
            ChooseAddress = itemView.findViewById(R.id.ChooseAddress);
        }
    }

    public interface OnAddressActionListener {
        void onRemoveAddress(String addressId);
        void onEditAddress(Address address);
        void onChooseAddress(Address address);
    }
}
