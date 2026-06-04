package com.example.optica.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("uid")
    private String uid;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("role")
    private String role; // "admin" o "cliente"

    public User(String uid, String email, String name, String role) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public String getUid() { return uid; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getRole() { return role; }
}
