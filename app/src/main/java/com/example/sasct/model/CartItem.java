package com.example.sasct.model;

import com.google.firebase.firestore.PropertyName;

public class CartItem {

    @PropertyName("offertId")
    private String offertId;

    @PropertyName("name")
    private String name;

    @PropertyName("image_url")
    private String image_url;

    @PropertyName("price")
    private double price;

    @PropertyName("quantity")
    private int quantity;

    public CartItem() {
        // Firestore needs the empty constructor
    }

    public CartItem(String offertId, String name, String image_url, double price, int quantity) {
        this.offertId = offertId;
        this.name = name;
        this.image_url = image_url;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters and setters
    public String getOffertId() { return offertId; }
    public void setOffertId(String offertId) { this.offertId = offertId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getImageUrl() {
        return image_url;
    }

    public void setImageUrl(String image_url) {
        this.image_url = image_url;
    }
}
