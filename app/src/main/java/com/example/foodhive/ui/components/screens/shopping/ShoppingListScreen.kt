package com.example.foodhive.ui.components.screens.shopping

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.foodhive.ui.components.scaffold.MainScaffold

@Composable
fun ShoppingListScreen(navController: NavController) {
    MainScaffold(navController = navController, currentScreen = "Shopping List") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "🛒 Your Shopping List",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("This page is ready for your grocery goals! 💡")
        }
    }
}
