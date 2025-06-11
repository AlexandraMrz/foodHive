package com.example.foodhive.ui.components.dialogs

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    productId: String,
    currentName: String,
    currentCategory: String,
    currentQuantity: Int,
    currentWeight: String,
    currentExpDate: String,
    onDismiss: () -> Unit,
    onUpdateSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val uid = auth.currentUser?.uid ?: return

    var name by remember { mutableStateOf(currentName) }
    var category by remember { mutableStateOf(currentCategory) }
    var quantity by remember { mutableStateOf(currentQuantity) }
    var weight by remember { mutableStateOf(currentWeight) }
    var expDate by remember { mutableStateOf(currentExpDate) }
    var expanded by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val categories = listOf("Fruits", "Vegetables", "Dairy", "Bakery", "Meat", "Beverages", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank() || category.isBlank() || expDate.isBlank()) {
                    scope.launch {
                        // Optional SnackbarHost integration if needed
                    }
                    return@TextButton
                }
                val updatedProduct = mapOf(
                    "name" to name,
                    "category" to category,
                    "quantity" to quantity.toString(),
                    "weight" to weight,
                    "expDate" to expDate
                )

                db.collection("users").document(uid)
                    .collection("products").document(productId)
                    .update(updatedProduct)
                    .addOnSuccessListener {
                        onUpdateSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Update failed", e)
                    }
            }) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Edit Product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    category = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = quantity.toString(),
                    onValueChange = { quantity = it.toIntOrNull() ?: 1 },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(onClick = {
                    DatePickerDialog(context, { _, year, month, day ->
                        calendar.set(year, month, day)
                        expDate = dateFormatter.format(calendar.time)
                    },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Text(if (expDate.isBlank()) "Pick Expiration Date" else "Exp Date: $expDate")
                }
            }
        }
    )
}
