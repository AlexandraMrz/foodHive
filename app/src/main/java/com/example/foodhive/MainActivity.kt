package com.example.foodhive

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.foodhive.auth.AppThemeMode
import com.example.foodhive.auth.ForgotPasswordScreen
import com.example.foodhive.auth.SettingsScreen
import com.example.foodhive.auth.SignInScreen
import com.example.foodhive.ui.components.screens.auth.ProfileEditScreen
import com.example.foodhive.ui.components.screens.auth.ProfileScreen
import com.example.foodhive.ui.components.screens.auth.SignUpScreen
import com.example.foodhive.ui.components.screens.history.HistoryScreen
import com.example.foodhive.ui.components.screens.home.HomeScreen
import com.example.foodhive.ui.components.screens.product.AddProductFormScreen
import com.example.foodhive.ui.components.screens.product.FavScreen
import com.example.foodhive.ui.components.screens.products.ProductsDashboardScreen
import com.example.foodhive.ui.components.screens.recipes.RecipeBotScreen
import com.example.foodhive.ui.components.screens.scanner.BarcodeScannerScreen
import com.example.foodhive.ui.components.screens.scanner.OcrScannerScreen
import com.example.foodhive.ui.components.screens.shopping.ShoppingListScreen
import com.example.foodhive.ui.theme.FoodHiveTheme
import com.example.foodhive.ui.utils.ThemePreferencesManager
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Notifications permission denied. You won't receive alerts.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permission for notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                ) {
                    Toast.makeText(
                        this,
                        "Please allow notifications so we can alert you about expiring food or useful tips.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val context = applicationContext
            val themeManager = remember { ThemePreferencesManager(context) }
            val themeMode by themeManager.themeModeFlow.collectAsState(initial = AppThemeMode.SYSTEM.name)

            val isDarkTheme = when (AppThemeMode.valueOf(themeMode)) {
                AppThemeMode.DARK -> true
                AppThemeMode.LIGHT -> false
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            FoodHiveTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }

                val isUserLoggedIn = FirebaseAuth.getInstance().currentUser != null
                val startDestination = if (isUserLoggedIn) Screen.Home.route else Screen.SignIn.route

                BackHandler(enabled = true) {
                    val currentRoute = navController.currentBackStackEntry?.destination?.route
                    when (currentRoute) {
                        Screen.Home.route, Screen.SignIn.route -> finish()
                        else -> navController.popBackStack()
                    }
                }

                NavHost(navController = navController, startDestination = startDestination) {

                    // Auth Screens
                    composable(Screen.SignIn.route) {
                        SignInScreen(
                            onSignInSuccess = { navController.navigate(Screen.Home.route) },
                            onGoogleSignIn = { /* TODO */ },
                            onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                            onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
                        )
                    }
                    composable(Screen.SignUp.route) {
                        SignUpScreen(
                            onSignUpSuccess = { navController.navigate(Screen.Home.route) },
                            onNavigateToSignIn = { navController.navigate(Screen.SignIn.route) }
                        )
                    }
                    composable(Screen.ForgotPassword.route) {
                        ForgotPasswordScreen(onBackToSignIn = { navController.navigate(Screen.SignIn.route) })
                    }

                    // Main Screens
                    composable(Screen.Home.route) { HomeScreen(navController) }
                    composable(Screen.Profile.route) { ProfileScreen(navController) }
                    composable(Screen.EditProfile.route) { ProfileEditScreen(navController) }
                    composable(Screen.Products.route) { ProductsDashboardScreen(navController) }
                    composable(Screen.ShoppingList.route) { ShoppingListScreen(navController) }

                    composable(Screen.AddProduct.route,
                        arguments = listOf(navArgument("barcode") {
                            nullable = true
                            defaultValue = ""
                        })
                    ) { entry ->
                        val barcode = entry.arguments?.getString("barcode")
                        AddProductFormScreen(
                            navController = navController,
                            scannedBarcode = barcode,
                            snackbarHostState = snackbarHostState // ✅ FIXED
                        )
                    }

                    composable(Screen.BarcodeScanner.route) { BarcodeScannerScreen(navController) }
                    composable(Screen.OcrScanner.route) { OcrScannerScreen(navController) }
                    composable(Screen.Settings.route) { SettingsScreen(navController) }
                    composable(Screen.History.route) { HistoryScreen(navController) }
                    composable(Screen.Favourites.route) { FavScreen(navController) }
                    composable(Screen.RecipeBot.route) { RecipeBotScreen(navController) }
                }
            }
        }
    }
}
