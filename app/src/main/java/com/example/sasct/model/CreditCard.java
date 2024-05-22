package com.example.sasct.model;

import java.io.Serializable;

public class CreditCard implements Serializable {
    private String id; // Auto-generated ID
    private String cardNumber; // Sensitive information might be truncated or hashed
    private String cardName;
    private String expiration;
    private String cvv;

    public CreditCard() {}

    public CreditCard(String id, String cardNumber, String cardName, String expiration, String cvv) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.cardName = cardName;
        this.expiration = expiration;
        this.cvv = cvv;
    }

    // Getters and setters for all fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getCardName() { return cardName; }
    public void setCardName(String cardName) { this.cardName = cardName; }

    public String getExpiration() { return expiration; }
    public void setExpiration(String expiration) { this.expiration = expiration; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
}