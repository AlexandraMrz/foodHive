package com.example.foodhive.ui.components.screens.product;

import com.example.foodhive.ui.components.screens.product.ProductRepo;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProductModel {
    private String id;
    private String name;
    private String category;
    private long addDate;
    private long expDate;
    private int quantity;
    private double weight;

    //no-arg constructor
    public void Product(){

    }
    //parameterized constructor
    public void Product(String id, String name, String category, long addDate, long expDate, int quantity, double weight)
    {
        this.id = id;
        this.name = name;
        this.category = category;
        this.addDate = addDate;
        this.expDate = expDate;
        this.quantity = quantity;
        this.weight = weight;
    }
    //getters and setters
    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getCategory(){
        return category;
    }
    public void setCategory(String category){
        this.category = category;
    }
    public long getAddDate(){
        return addDate;
    }
    public void setAddDate(long addDate){
        this.addDate = addDate;
    }
    public long getExpDate(){
        return expDate;
    }
    public void setExpDate(long expDate){
        this.expDate = expDate;
    }
    public int getQuantity(){
        return quantity;
    }
    public void setQuantity(int quantity){
        this.quantity = quantity;
    }
    public double getWeight(){
        return weight;
    }
    public void setWeight(double weight){
        this.weight = weight;
    }
}
