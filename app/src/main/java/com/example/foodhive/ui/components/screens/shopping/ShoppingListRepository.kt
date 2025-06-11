package com.example.foodhive.ui.components.screens.shopping

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object ShoppingListRepository{
    private val db = FirebaseFirestore.getInstance()

    suspend fun fetchShoppingItems(userId: String): List<ShoppingItem>{
        val snapshot = db.collection("users")
            .document(userId)
            .collection("shoppingList")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(ShoppingItem::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun getMissingIngredients(userId: String, recipeIngredients: List<String>): List<String> {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("shoppingList")
            .get()
            .await()

        val existingNames = snapshot.documents
            .filter { it.getBoolean("bought") != true }
            .mapNotNull { it.getString("name")?.trim()?.lowercase() }
            .toSet()

        return recipeIngredients.filter {
            it.trim().lowercase() !in existingNames
        }
    }

    suspend fun toggleBought(userId: String, item: ShoppingItem) {
        db.collection("users")
            .document(userId)
            .collection("shoppingList")
            .document(item.id)
            .update("bought", !item.bought)
            .await()
    }

    suspend fun deleteItem(userId: String, itemId: String){
        db.collection("users")
            .document(userId)
            .collection("shoppingList")
            .document(itemId)
            .delete()
            .await()
    }

    suspend fun addItem(userId: String, item: ShoppingItem){
        db.collection("users")
            .document(userId)
            .collection("shoppingList")
            .add(item)
            .await()
    }

    suspend fun addMissingItems(userId: String, missingItems: List<String>) {
        val batch = db.batch()
        val listRef = db.collection("users").document(userId).collection("shoppingList")

        missingItems.forEach { itemName ->
            val newDoc = listRef.document()
            val item = mapOf(
                "id" to newDoc.id,
                "name" to itemName,
                "quantity" to 1,
                "bought" to false,
                "category" to getAutoCategory(itemName) // auto-categorize
            )
            batch.set(newDoc, item)
        }

        batch.commit().await()
    }
    suspend fun updateItem(userId: String, item: ShoppingItem) {
        db.collection("users")
            .document(userId)
            .collection("shoppingList")
            .document(item.id)
            .set(item)
            .await()
    }

    private fun getAutoCategory(name: String): String {
        val lower = name.trim().lowercase()
        return when {
            listOf("milk", "cheese", "yogurt", "butter").any { lower.contains(it) } -> "Dairy"
            listOf("apple", "banana", "orange", "berries", "grapes").any { lower.contains(it) } -> "Fruits"
            listOf("carrot", "tomato", "lettuce", "potato", "onion").any { lower.contains(it) } -> "Vegetables"
            listOf("bread", "croissant", "brioche", "bun").any { lower.contains(it) } -> "Bakery"
            listOf("soda", "cola", "juice", "water").any { lower.contains(it) } -> "Drinks"
            listOf("chips", "chocolate", "cookie", "candy").any { lower.contains(it) } -> "Snacks"
            else -> "Other"
        }
    }
}

