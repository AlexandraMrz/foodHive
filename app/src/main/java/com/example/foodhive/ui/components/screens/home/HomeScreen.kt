package com.example.foodhive.ui.components.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.foodhive.ui.components.scaffold.MainScaffold
import com.google.firebase.auth.FirebaseAuth


@Composable
fun HomeScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    // 👇 WRAP everything with MainScaffold to get drawer + top bar
    MainScaffold(navController = navController, currentScreen = "Home") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to FoodHive!",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            user?.email?.let {
                Text(text = "Logged in as: $it")
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { navController.navigate("profile") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Profile")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate("signIn") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}
