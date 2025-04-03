package com.example.foodhive.ui.components.screens.product;

import android.util.Log;

import com.example.foodhive.ui.components.screens.products.Product;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProductRepo {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addProduct() {
        Product product = new Product(
                "Fresh Apple",      // name
                "Fruits",           // category
                "2025-04-02",       // addDate (use your date format)
                "2025-04-10",       // expDate
                "3",                // quantity
                "0.5kg",            // weight
                "Organic apples"    // note
        );

        db.collection("products")
                .add(product)
                .addOnSuccessListener(documentReference ->
                        Log.d("Firestore", "Product added with ID: " + documentReference.getId())
                )
                .addOnFailureListener(e ->
                        Log.w("Firestore", "Error adding product", e)
                );
    }
}
