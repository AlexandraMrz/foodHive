package com.example.foodhive.data.remote
import retrofit2.http.GET
import retrofit2.http.Path

data class OpenFoodProductResponse(
    val product: ProductData?,
    val status: Int,
    val code: String
)

data class ProductData(
    val product_name: String?,
    val categories: String?,
    val quantity: String?
)

interface OpenFoodFactsApi{
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProduct(@Path("barcode") barcode: String?): OpenFoodProductResponse
}