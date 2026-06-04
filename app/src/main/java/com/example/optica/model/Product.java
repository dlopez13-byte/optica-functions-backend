package com.example.optica.model;

import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("category")
    private String category;
    
    @SerializedName("imageUrl")
    private String imageUrl;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("price")
    private double price;

    public Product(String id, String name, String category, String imageUrl, String description, double price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
        this.description = description;
        this.price = price;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
}
