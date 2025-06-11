package com.example.foodhive.ui.components.screens.shopping
import java.util.UUID

data class ShoppingItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val quantity: Int = 1,
    var bought: Boolean = false,
    val category: String? = null
)
