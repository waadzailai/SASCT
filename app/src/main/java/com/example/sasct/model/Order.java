package com.example.sasct.model;

import java.util.List;

public class Order {
    private String id;
    private List<CartItem> cartItems;
    private Address address;
    private CreditCard card; // Add a field for the selected card
    private double totalPrice;
    private String status = "In Process";
    private String date = "17/05/2024";

    public Order() {}

    public Order(List<CartItem> cartItems, Address address, CreditCard card, double totalPrice) {
        this.cartItems = cartItems;
        this.address = address;
        this.card = card;
        this.totalPrice = totalPrice;
    }

    public List<CartItem> getCartItems() { return cartItems; }
    public void setCartItems(List<CartItem> cartItems) { this.cartItems = cartItems; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}