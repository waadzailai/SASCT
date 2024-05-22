package com.example.sasct.model;

import java.io.Serializable;

public class NegotiatedOffer implements Serializable {
    private String offerId;
    private String firestoreId;
    private String buyerId;
    private String supplierId;
    private double initialPrice;
    private int initialQuantity;
    private double negotiatedPrice;
    private int negotiatedQuantity;
    private String status; // e.g., "Pending", "Accepted", "Rejected", "Counter-proposed"

    // No-argument constructor
    public NegotiatedOffer() {
        // Initialize fields to default values if needed
    }

    // Constructor
    public NegotiatedOffer(String offerId, String buyerId, String supplierId, double initialPrice, int initialQuantity, double negotiatedPrice, int negotiatedQuantity, String status) {
        this.offerId = offerId;
        this.buyerId = buyerId;
        this.supplierId = supplierId;
        this.initialPrice = initialPrice;
        this.initialQuantity = initialQuantity;
        this.negotiatedPrice = negotiatedPrice;
        this.negotiatedQuantity = negotiatedQuantity;
        this.status = status;
    }

//    protected NegotiatedOffer(Parcel in) {
//        offerId = in.readString();
//        buyerId = in.readString();
//        supplierId = in.readString();
//        initialPrice = in.readDouble();
//        initialQuantity = in.readInt();
//        negotiatedPrice = in.readDouble();
//        negotiatedQuantity = in.readInt();
//        status = in.readString();
//    }

    // Getters and Setters
    public String getFirestoreId() { return firestoreId; }

    public void setFirestoreId(String firestoreId) { this.firestoreId = firestoreId;}

    public String getOfferId() { return offerId; }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getBuyerId() { return buyerId; }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public double getInitialPrice() {
        return initialPrice;
    }

    public void setInitialPrice(double initialPrice) {
        this.initialPrice = initialPrice;
    }

    public int getInitialQuantity() {
        return initialQuantity;
    }

    public void setInitialQuantity(int initialQuantity) {
        this.initialQuantity = initialQuantity;
    }

    public double getNegotiatedPrice() {
        return negotiatedPrice;
    }

    public void setNegotiatedPrice(double negotiatedPrice) {
        this.negotiatedPrice = negotiatedPrice;
    }

    public int getNegotiatedQuantity() {
        return negotiatedQuantity;
    }

    public void setNegotiatedQuantity(int negotiatedQuantity) {
        this.negotiatedQuantity = negotiatedQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
