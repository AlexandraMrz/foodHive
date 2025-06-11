package com.example.foodhive.ui.components.screens.product

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FavouritesRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun fetchFavourites(userId: String): List<FavouriteItem> {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("favorites")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val ingredients = doc.get("ingredients")
            val ingredientList = if (ingredients is List<*>) {
                ingredients.filterIsInstance<String>()
            } else emptyList()

            val id = doc.getLong("id")?.toInt() ?: return@mapNotNull null
            val title = doc.getString("title") ?: return@mapNotNull null
            val image = doc.getString("image") ?: ""
            val instructions = doc.getString("instructions") ?: ""
            val sourceUrl = doc.getString("sourceUrl") ?: ""

            FavouriteItem(
                id = id,
                title = title,
                image = image,
                ingredients = ingredientList,
                instructions = instructions,
                sourceUrl = sourceUrl
            )
        }
    }
}
