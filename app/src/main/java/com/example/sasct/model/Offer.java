package com.example.sasct.model;

import java.io.Serializable;

public class Offer implements Serializable {
    private String id; // Auto-generated ID
    private String userId; // User ID (foreign key)
    private String title;
    private String description;
    private String category;
    private String shipment;
    private String type;
    private double price;
    private int quantity;
    private String imageUrl;

    // Default constructor (required for Firestore)
    public Offer() {
    }

    public Offer(String userId, String title, String description, String category, String shipment, double price, int quantity, String imageUrl) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.shipment = shipment;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;

        this.type = "public";
    }

    public Offer(String userId, String title, String description, String category, String shipment, double price, int quantity, String imageUrl, String type) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.shipment = shipment;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.type = "private";
    }

    // Getters and setters for all fields
    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getShipment() {
        return shipment;
    }

    public void setShipment(String shipment) {
        this.shipment = shipment;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
