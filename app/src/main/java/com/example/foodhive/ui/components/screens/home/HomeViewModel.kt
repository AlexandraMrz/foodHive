package com.example.foodhive.ui.components.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodhive.ui.components.screens.product.ProductModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _productList = MutableStateFlow<List<ProductModel>>(emptyList())
    val productList: StateFlow<List<ProductModel>> = _productList

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val snapshot = db.collection("users").document(uid).collection("products").get().await()

                println("DEBUG: Found ${snapshot.documents.size} user products")

                val items = snapshot.documents.map { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val name = doc.getString("name") ?: "Unnamed"
                    val category = doc.getString("category") ?: "Uncategorized"
                    val addDate = doc.getString("addDate") ?: ""
                    val expDate = doc.getString("expDate") ?: ""
                    val quantity = doc.getString("quantity") ?: "1"
                    val weight = doc.getString("weight") ?: "Unknown"

                    ProductModel(id, name, category, addDate, expDate, quantity, weight)
                }

                println("DEBUG: Mapped ${items.size} products")
                _productList.value = items
            } catch (e: Exception) {
                println("DEBUG ERROR: ${e.message}")
                _productList.value = emptyList()
            }
        }
    }


}
