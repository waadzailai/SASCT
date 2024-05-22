package com.example.sasct.model;

import com.example.sasct.model.Offer;

import java.util.List;

public class User {
    private String id; // User ID
    private String email;
    private String role;
    private String companyName;
    private String imageUrl; // Assuming image is stored as a string path or URL
    private String password; // Storing password for simplicity, in practice, password handling should be more secure

    public User() {
        // Required empty public constructor for Firestore
    }

    public User(String id, String email, String role, String companyName) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.companyName = companyName;
    }

    public User(String id, String email, String companyName) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.companyName = companyName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
