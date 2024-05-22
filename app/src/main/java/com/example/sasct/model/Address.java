package com.example.sasct.model;

import java.io.Serializable;

public class Address implements Serializable {
    private String id; // Unique address ID
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String additionalInfo;

    public Address() {
        // Default constructor for Firestore
    }

    public Address(String id, String street, String city, String state, String postalCode, String country, String additionalInfo) {
        this.id = id;
        this.street = street;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.additionalInfo = additionalInfo;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }
}