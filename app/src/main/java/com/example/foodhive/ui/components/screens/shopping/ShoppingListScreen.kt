package com.example.foodhive.ui.components.screens.shopping

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.foodhive.ui.components.scaffold.MainScaffold
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ShoppingListScreen(navController: NavController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val shoppingItems = remember { mutableStateListOf<ShoppingItem>() }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var itemBeingEdited by remember { mutableStateOf<ShoppingItem?>(null) }
    var selectedCategory by remember { mutableStateOf("All") }

    LaunchedEffect(userId) {
        userId?.let {
            val items = ShoppingListRepository.fetchShoppingItems(it)
            shoppingItems.clear()
            shoppingItems.addAll(items)
        }
    }

    MainScaffold(navController = navController, currentScreen = "Shopping List") {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("Shopping List", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                val categories = listOf("All") + shoppingItems.mapNotNull { it.category }.distinct()
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(categories) { category ->
                        OutlinedButton(
                            onClick = { selectedCategory = category },
                            enabled = selectedCategory != category
                        ) {
                            Text(category)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filteredItems = if (selectedCategory == "All") shoppingItems else shoppingItems.filter { it.category == selectedCategory }
                    val groupedItems = filteredItems.groupBy { it.category ?: "Other" }

                    groupedItems.forEach { (category, itemsInCategory) ->
                        item {
                            Text(category, style = MaterialTheme.typography.titleMedium)
                        }

                        items(itemsInCategory, key = { it.id }) { item ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                            ) {
                                ShoppingListItemCard(
                                    item = item,
                                    onToggleBought = {
                                        userId?.let { uid ->
                                            CoroutineScope(Dispatchers.IO).launch {
                                                ShoppingListRepository.toggleBought(uid, item)
                                                withContext(Dispatchers.Main) {
                                                    val index = shoppingItems.indexOfFirst { it.id == item.id }
                                                    if (index != -1) {
                                                        shoppingItems[index] = item.copy(bought = !item.bought)
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    onDelete = {
                                        userId?.let { uid ->
                                            CoroutineScope(Dispatchers.IO).launch {
                                                ShoppingListRepository.deleteItem(uid, item.id)
                                                withContext(Dispatchers.Main) {
                                                    shoppingItems.removeAll { it.id == item.id }
                                                }
                                            }
                                        }
                                    },
                                    onEdit = {
                                        itemBeingEdited = item
                                        showEditDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }

            if (showAddDialog) {
                AddShoppingItemDialog(
                    onDismiss = { showAddDialog = false },
                    onAdd = { name, qty, category ->
                        userId?.let {
                            val newItem = ShoppingItem(name = name, quantity = qty, category = category)
                            CoroutineScope(Dispatchers.IO).launch {
                                ShoppingListRepository.addItem(it, newItem)
                                withContext(Dispatchers.Main) {
                                    shoppingItems.add(newItem.copy())
                                }
                            }
                        }
                    }
                )
            }

            if (showEditDialog && itemBeingEdited != null) {
                AddShoppingItemDialog(
                    onDismiss = {
                        showEditDialog = false
                        itemBeingEdited = null
                    },
                    onAdd = { newName, newQty, newCategory ->
                        userId?.let { uid ->
                            val updatedItem = itemBeingEdited!!.copy(
                                name = newName,
                                quantity = newQty,
                                category = newCategory
                            )
                            CoroutineScope(Dispatchers.IO).launch {
                                ShoppingListRepository.updateItem(uid, updatedItem)
                                withContext(Dispatchers.Main) {
                                    val index = shoppingItems.indexOfFirst { it.id == updatedItem.id }
                                    if (index != -1) shoppingItems[index] = updatedItem
                                    showEditDialog = false
                                    itemBeingEdited = null
                                }
                            }
                        }
                    },
                    initialName = itemBeingEdited!!.name,
                    initialQty = itemBeingEdited!!.quantity.toString(),
                    initialCategory = itemBeingEdited!!.category ?: "Other"
                )
            }
        }
    }
}

@Composable
fun ShoppingListItemCard(
    item: ShoppingItem,
    onToggleBought: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(
            containerColor = if (item.bought)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (item.bought) TextDecoration.LineThrough else TextDecoration.None
                    )
                )
                Text("Qty: ${item.quantity}", style = MaterialTheme.typography.bodySmall)
                item.category?.let {
                    Text("Category: $it", style = MaterialTheme.typography.labelSmall)
                }
            }

            Row {
                IconButton(onClick = onToggleBought) {
                    Icon(Icons.Default.Check, contentDescription = "Toggle Bought", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AddShoppingItemDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, String) -> Unit,
    initialName: String = "",
    initialQty: String = "1",
    initialCategory: String = "Other"
) {
    var name by remember { mutableStateOf(initialName) }
    var quantity by remember { mutableStateOf(initialQty) }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    val categories = listOf("Dairy", "Vegetables", "Fruits", "Snacks", "Drinks", "Bakery", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val qty = quantity.toIntOrNull() ?: 1
                if (name.isNotBlank()) {
                    onAdd(name.trim(), qty, selectedCategory)
                    onDismiss()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text("Shopping Item", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Text("Category", style = MaterialTheme.typography.labelMedium)
                Box {
                    var expanded by remember { mutableStateOf(false) }

                    OutlinedButton(onClick = { expanded = true }) {
                        Text(selectedCategory)
                    }

                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}
