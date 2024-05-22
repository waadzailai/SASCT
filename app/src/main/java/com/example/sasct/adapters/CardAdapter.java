package com.example.sasct.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.sasct.R;
import com.example.sasct.model.CreditCard;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
    private List<CreditCard> cards;
    private Context context;
    private OnCardActionListener actionListener;
    private String activeCardId; // Track the selected card ID

    public CardAdapter(Context context, List<CreditCard> cards, OnCardActionListener actionListener, String activeCardId) {
        this.context = context;
        this.cards = cards;
        this.actionListener = actionListener;
        this.activeCardId = activeCardId;
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.credit_card, parent, false); // Ensure this layout exists
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        CreditCard card = cards.get(position);

        holder.cardNumberTextView.setText(card.getCardNumber());
        holder.cardNameTextView.setText(card.getCardName());
        holder.expirationTextView.setText(card.getExpiration());

        holder.removeCardButton.setOnClickListener(v -> actionListener.onRemoveCard(card.getId()));
        holder.editCardButton.setOnClickListener(v -> actionListener.onEditCard(card));
        holder.itemView.setOnClickListener(v -> {
            activeCardId = card.getId(); // Set as active card
            actionListener.onChooseCard(card);
            notifyDataSetChanged(); // Refresh the UI
        });

        if (activeCardId != null && activeCardId.equals(card.getId())) {
            holder.selectedTick.setVisibility(View.VISIBLE); // Show tick
        } else {
            holder.selectedTick.setVisibility(View.GONE); // Hide tick
        }
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {
        TextView cardNumberTextView;
        TextView cardNameTextView;
        TextView expirationTextView;
        ImageView removeCardButton;
        ImageView editCardButton;
        ImageView selectedTick;

        public CardViewHolder(View itemView) {
            super(itemView);

            cardNumberTextView = itemView.findViewById(R.id.cardNumberTextView);
            cardNameTextView = itemView.findViewById(R.id.cardNameTextView);
            expirationTextView = itemView.findViewById(R.id.expirationTextView);
            removeCardButton = itemView.findViewById(R.id.removeCardButton);
            editCardButton = itemView.findViewById(R.id.editCardButton);
            selectedTick = itemView.findViewById(R.id.selectedTick);
        }
    }

    public interface OnCardActionListener {
        void onRemoveCard(String cardId);
        void onEditCard(CreditCard card);
        void onChooseCard(CreditCard card);
    }
}
