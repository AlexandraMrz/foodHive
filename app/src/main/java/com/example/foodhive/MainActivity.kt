package com.example.foodhive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foodhive.auth.SignInScreen
import com.example.foodhive.ui.components.screens.auth.ForgotPasswordScreen
import com.example.foodhive.ui.components.screens.auth.ProfileScreen
import com.example.foodhive.ui.components.screens.auth.SignUpScreen
import com.example.foodhive.ui.components.screens.auth.SplashScreen
import com.example.foodhive.ui.components.screens.home.HomeScreen
import com.example.foodhive.ui.components.screens.product.AddProductFormScreen
import com.example.foodhive.ui.components.screens.products.ProductsDashboardScreen
import com.example.foodhive.ui.components.screens.shopping.ShoppingListScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            val startDestination = if (FirebaseAuth.getInstance().currentUser != null) "home" else "signIn"

            NavHost(navController, startDestination = "splash") {
                composable("splash"){
                    SplashScreen(navController)
                }
                composable("signIn") {
                    SignInScreen(
                        onSignInSuccess = { navController.navigate("home") },
                        onGoogleSignIn = { /* Implement Google Sign-In */ },
                        onNavigateToSignUp = { navController.navigate("signUp") },
                        onNavigateToForgotPassword = { navController.navigate("forgotPassword") }  // navigation
                    )
                }
                composable("signUp") {
                    SignUpScreen(
                        onSignUpSuccess = { navController.navigate("home") },
                        onNavigateToSignIn = { navController.navigate("signIn") }
                    )
                }
                composable("forgotPassword") {  // forgot password Screen
                    ForgotPasswordScreen(
                        onBackToSignIn = { navController.navigate("signIn") }
                    )
                }
                composable("home") {
                    HomeScreen(navController)
                }
                composable("profile"){
                    ProfileScreen(navController)
                }
                composable("products"){
                    ProductsDashboardScreen(navController)
                }
                composable("shoppingList"){
                    ShoppingListScreen(navController)
                }
                composable("addProduct") {
                    AddProductFormScreen(navController)
                }
            }
        }
    }
}
