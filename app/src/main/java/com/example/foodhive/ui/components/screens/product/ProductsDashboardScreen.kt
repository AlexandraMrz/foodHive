package com.example.foodhive.ui.components.screens.products

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.foodhive.ui.components.dialogs.AddProductOptionDialog
import com.example.foodhive.ui.components.dialogs.EditProductDialog
import com.example.foodhive.ui.components.scaffold.MainScaffold
import com.example.foodhive.ui.utils.getExpirationColor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ✅ Combined data class
data class Product(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val addDate: String = "",
    val expDate: String = "",
    val quantity: String = "",
    val weight: String = ""
)

// ✅ Combined ProductCard
@Composable
fun ProductCard(
    product: Product,
    isFavourite: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavourite: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val expirationColor = getExpirationColor(product.expDate)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = expirationColor
        ),
        elevation = CardDefaults.elevatedCardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Product Name + Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = "Product Icon",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Action Buttons
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = onToggleFavourite) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favourite",
                            tint = if (isFavourite) Color.Red else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product Info
            Text("Category: ${product.category}", style = MaterialTheme.typography.labelMedium)

            if (product.quantity.isNotBlank()) {
                Text("Quantity: ${product.quantity}", style = MaterialTheme.typography.bodySmall)
            }
            if (product.weight.isNotBlank()) {
                Text("Weight: ${product.weight}", style = MaterialTheme.typography.bodySmall)
            }
            if (product.expDate.isNotBlank()) {
                Text("Expires: ${product.expDate}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Product") },
            text = { Text("Are you sure you want to delete \"${product.name}\"?") }
        )
    }
}
// ✅ Main screen composable
@Composable
fun ProductsDashboardScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf("All") }
    var products by remember { mutableStateOf(listOf<Product>()) }
    var favouriteNames by remember { mutableStateOf(setOf<String>()) }
    var showAddModal by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    val categories = listOf("All", "Dairy", "Fruits", "Vegetables", "Snacks", "Drinks", "Bakery", "Meat")

    LaunchedEffect(uid, selectedCategory) {
        uid?.let {
            db.collection("users").document(it).collection("products")
                .addSnapshotListener { snapshot, _ ->
                    val result = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Product::class.java)?.copy(id = doc.id)
                    } ?: emptyList()
                    products = if (selectedCategory == "All") result else result.filter { it.category == selectedCategory }
                }

            val snapshot = db.collection("users").document(it).collection("favourites").get().await()
            favouriteNames = snapshot.documents.mapNotNull { doc -> doc.getString("name") }.toSet()
        }
    }

    MainScaffold(navController = navController, currentScreen = "Products") {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                    items(products) { product ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(300)) + slideInVertically(initialOffsetY = { it / 2 })
                        ) {
                            ProductCard(
                                product = product,
                                isFavourite = favouriteNames.contains(product.name),
                                onEdit = {
                                    selectedProduct = product
                                    showEditDialog = true
                                },
                                onDelete = {
                                    uid?.let { userId ->
                                        db.collection("users")
                                            .document(userId)
                                            .collection("products")
                                            .document(product.id)
                                            .delete()
                                            .addOnSuccessListener {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Product deleted.")
                                                }
                                            }
                                            .addOnFailureListener {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Failed to delete product.")
                                                }
                                            }
                                    }
                                },
                                onToggleFavourite = {
                                    // Handle toggle
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(onClick = { showAddModal = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ Add Product")
                }

                if (showAddModal) {
                    AddProductOptionDialog(
                        onManualEntry = {
                            showAddModal = false
                            navController.navigate("addProduct")
                        },
                        onScanEntry = {
                            showAddModal = false
                            navController.navigate("barcodeScanner")
                        },
                        onOcrEntry = {
                            showAddModal = false
                            navController.navigate("ocrScanner")
                        },
                        onAiImageEntry = {
                            showAddModal = false
                            navController.navigate("addProduct?mode=image")
                        },
                        onDismiss = {
                            showAddModal = false
                        }
                    )
                }

                if (showEditDialog && selectedProduct != null) {
                    EditProductDialog(
                        productId = selectedProduct!!.id,
                        currentName = selectedProduct!!.name,
                        currentCategory = selectedProduct!!.category,
                        currentQuantity = selectedProduct!!.quantity.toIntOrNull() ?: 1,
                        currentWeight = selectedProduct!!.weight,
                        currentExpDate = selectedProduct!!.expDate,
                        onDismiss = {
                            showEditDialog = false
                            selectedProduct = null
                        },
                        onUpdateSuccess = {
                            showEditDialog = false
                            selectedProduct = null
                        }
                    )
                }
            }
        }
    }
}
