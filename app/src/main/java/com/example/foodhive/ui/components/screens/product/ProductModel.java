package com.example.foodhive.ui.components.screens.product;

public class ProductModel {
    private String id;
    private String name;
    private String category;
    private String addDate;
    private String expDate;
    private String quantity;
    private String weight;

    // Required no-arg constructor for Firestore
    public ProductModel() {}

    // Full constructor
    public ProductModel(String id, String name, String category, String addDate, String expDate, String quantity, String weight) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.addDate = addDate;
        this.expDate = expDate;
        this.quantity = quantity;
        this.weight = weight;
    }

    // Getters
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getCategory() {
        return category;
    }
    public String getAddDate() {
        return addDate;
    }
    public String getExpDate() {
        return expDate;
    }
    public String getQuantity() {
        return quantity;
    }
    public String getWeight() {
        return weight;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public void setAddDate(String addDate) {
        this.addDate = addDate;
    }
    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
    public void setWeight(String weight) {
        this.weight = weight;
    }
}
