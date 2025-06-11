package com.example.foodhive.ui.components.screens.product

data class FavouriteItem(
    val id: Int = 0,
    val title: String = "",
    val image: String = "",
    val ingredients: List<String> = emptyList(),
    val instructions: String = "",
    val sourceUrl: String = ""
)




