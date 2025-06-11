package com.example.foodhive

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object SignIn : Screen("signIn")
    object SignUp : Screen("signUp")
    object ForgotPassword : Screen("forgotPassword")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object EditProfile : Screen("editProfile")
    object Products : Screen("products")
    object AddProduct : Screen("addProduct?barcode={barcode}") {
        fun withBarcode(barcode: String?) = "addProduct?barcode=${barcode ?: ""}"
    }
    object ShoppingList : Screen("shoppingList")
    object BarcodeScanner : Screen("barcodeScanner")
    object OcrScanner : Screen("ocrScanner")
    object Settings : Screen("settings")
    object History : Screen("history")
    object Favourites : Screen("favourites")
    object RecipeBot : Screen("recipeBot")
}
