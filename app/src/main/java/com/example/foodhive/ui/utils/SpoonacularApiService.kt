package com.example.foodhive.ui.utils

import retrofit2.http.GET
import retrofit2.http.Query

data class SpoonacularIngredient(
    val name: String,
    val amount: Double,
    val unit: String
)

data class SpoonacularRecipeResponse(
    val id: Int,
    val title: String,
    val image: String,
    val usedIngredients: List<SpoonacularIngredient>,
    val missedIngredients: List<SpoonacularIngredient>
)
data class InstructionResponse(
    val name: String,
    val steps: List<InstructionStep>
)

data class InstructionStep(
    val number: Int,
    val step: String
)


interface SpoonacularApiService {
    @GET("recipes/findByIngredients")
    suspend fun findByIngredients(
        @Query("ingredients") ingredients: String,
        @Query("number") number: Int = 1,
        @Query("ranking") ranking: Int = 1,
        @Query("ignorePantry") ignorePantry: Boolean = true,
        @Query("apiKey") apiKey: String
    ): List<SpoonacularRecipeResponse>
    @GET("recipes/{id}/analyzedInstructions")
    suspend fun getInstructions(
        @retrofit2.http.Path("id") recipeId: Int,
        @Query("apiKey") apiKey: String
    ): List<InstructionResponse>

}

