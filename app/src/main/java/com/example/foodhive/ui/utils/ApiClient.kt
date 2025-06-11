package com.example.foodhive.ui.utils

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.spoonacular.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: SpoonacularApiService = retrofit.create(SpoonacularApiService::class.java)
}
