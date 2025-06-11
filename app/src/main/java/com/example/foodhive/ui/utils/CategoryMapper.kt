package com.example.foodhive.ui.utils

fun mapToAppCategory(apiCategory: String?): String {
    val normalized = apiCategory?.lowercase()?.trim() ?: ""

    return when {
        "meat" in normalized -> "Meat"
        "fruit" in normalized -> "Fruits"
        "vegetable" in normalized || "legume" in normalized -> "Vegetables"
        "snack" in normalized || "chips" in normalized -> "Snacks"
        "drink" in normalized || "soda" in normalized || "beverage" in normalized ||
                "boisson" in normalized || "juice" in normalized || "beer" in normalized -> "Drinks"
        "bread" in normalized || "bakery" in normalized || "pastry" in normalized -> "Bakery"
        "dairy" in normalized || "milk" in normalized || "cheese" in normalized || "yogurt" in normalized -> "Dairy"
        else -> "Other"
    }
}
