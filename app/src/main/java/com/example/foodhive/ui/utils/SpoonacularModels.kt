package com.example.foodhive.ui.utils

data class Ingredient(
    val name: String,
    val amount: Double,
    val unit: String
)

data class RecipeResponse(
    val id: Int,
    val title: String,
    val image: String,
    val usedIngredients: List<Ingredient>,
    val missedIngredients: List<Ingredient>
)

data class ComplexSearchResponse(
    val results: List<RecipeResponse>
)