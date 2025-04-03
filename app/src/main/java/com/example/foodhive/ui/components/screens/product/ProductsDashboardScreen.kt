package com.example.foodhive.ui.components.screens.products

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.foodhive.ui.components.scaffold.MainScaffold
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Product model
data class Product(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val addDate: String = "",
    val expDate: String = "",
    val quantity: String = "",
    val weight: String = ""
)

val categories = listOf("All", "Dairy","Meat", "Fruits", "Vegetables", "Snacks", "Drinks")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsDashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val scope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf("All") }
    var products by remember { mutableStateOf(listOf<Product>()) }
    var showModal by remember { mutableStateOf(false) }

    // Fetch products from Firestore
    LaunchedEffect(uid, selectedCategory) {
        uid?.let {
            db.collection("users").document(it).collection("products")
                .addSnapshotListener { snapshot, _ ->
                    val result = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Product::class.java)
                    } ?: emptyList()
                    products = if (selectedCategory == "All") result
                    else result.filter { it.category == selectedCategory }
                }
        }
    }

    MainScaffold(navController = navController, currentScreen = "Products") {
        Column(modifier = Modifier.padding(16.dp)) {
            // Horizontal scrollable category chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories.size) { index ->
                    val cat = categories[index]
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        modifier = Modifier
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Product grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(products) { product ->
                    ProductCard(product)
                }
            }
        }

        // Floating Add Product Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(onClick = { showModal = true }) {
                Text("+ Add Product")
            }
        }

        // Modal Sheet
        if (showModal) {
            ModalBottomSheet(onDismissRequest = { showModal = false }) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("How would you like to add a product?", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = {
                        showModal = false
                        navController.navigate("addProduct")
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Add Manually")
                    }
                    Button(onClick = {
                        showModal = false
                        Toast.makeText(context, "Scanner coming soon!", Toast.LENGTH_SHORT).show()
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Use Scanner")
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = product.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            if (product.quantity.isNotBlank()) {
                Text("Quantity: ${product.quantity}", style = MaterialTheme.typography.bodySmall)
            }
            if (product.weight.isNotBlank()) {
                Text("Weight: ${product.weight}", style = MaterialTheme.typography.bodySmall)
            }
            if (product.addDate.isNotBlank()) {
                Text("Added: ${product.addDate}", style = MaterialTheme.typography.bodySmall)
            }
            if (product.expDate.isNotBlank()) {
                Text("Expires: ${product.expDate}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

