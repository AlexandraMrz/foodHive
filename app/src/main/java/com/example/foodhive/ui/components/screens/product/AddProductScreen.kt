@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.foodhive.ui.components.screens.product

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.foodhive.ui.components.scaffold.MainScaffold
import com.example.foodhive.ui.utils.ActionButton
import com.example.foodhive.ui.utils.mapToAppCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddProductFormScreen(
    navController: NavController,
    scannedBarcode: String? = null,
    scannedText: String? = null,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Other") }
    var quantity by remember { mutableStateOf(1) }
    var weight by remember { mutableStateOf("") }
    var unitToggle by remember { mutableStateOf("auto") }
    var expDate by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val categories = listOf("Fruits", "Vegetables", "Dairy", "Bakery", "Meat", "Drinks", "Snacks", "Other")
    val calendar = Calendar.getInstance()
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            scope.launch {
                val label = processImage(it)
                if (label != null) {
                    name = label.replaceFirstChar { it.uppercase() }
                    category = mapToAppCategory(label)
                } else {
                    snackbarHostState.showSnackbar("❌ Could not detect product.")
                }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            scope.launch {
                val label = processImage(bitmap)
                if (label != null) {
                    name = label.replaceFirstChar { it.uppercase() }
                    category = mapToAppCategory(label)
                } else {
                    snackbarHostState.showSnackbar("❌ Could not detect product.")
                }
            }
        }
    }

    var invalidBarcodeSnackbarShown by remember(scannedBarcode) { mutableStateOf(false) }

    LaunchedEffect(scannedBarcode) {
        scannedBarcode?.let { barcode ->
            fetchProductInfoFromBarcode(context, barcode) { productName, rawCategory, rawWeight ->
                scope.launch {
                    if (productName.isNullOrBlank()) {
                        if (!invalidBarcodeSnackbarShown) {
                            snackbarHostState.showSnackbar("❌ Invalid barcode or product not found.")
                            invalidBarcodeSnackbarShown = true
                        }
                        return@launch
                    }
                    invalidBarcodeSnackbarShown = false

                    name = productName
                    category = mapToAppCategory(rawCategory ?: "Other")
                    weight = normalizeWeight(rawWeight ?: "")
                }
            }
        } ?: run {
            // Reset flag when barcode is cleared or null
            invalidBarcodeSnackbarShown = false
        }
    }

    LaunchedEffect(scannedText) {
        scannedText?.let {
            name = it.split(" ").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: ""
            quantity = Regex("\\d+").find(it)?.value?.toIntOrNull() ?: 1
            weight = normalizeWeight(it)
            expDate = Regex("\\d{4}-\\d{2}-\\d{2}").find(it)?.value ?: ""
        }
    }

    MainScaffold(
        navController = navController,
        currentScreen = "Add Product",
        snackbarHostState = snackbarHostState
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Add Product", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = category, onValueChange = {}, readOnly = true, label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach {
                        DropdownMenuItem(text = { Text(it) }, onClick = {
                            category = it
                            expanded = false
                        })
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = quantity.toString(), onValueChange = { quantity = it.toIntOrNull() ?: 1 },
                label = { Text("Quantity") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            val displayWeight = formatWeight(weight, unitToggle)
            OutlinedTextField(value = displayWeight, onValueChange = { weight = it }, label = { Text("Weight") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                listOf("auto", "g", "kg", "ml", "l").forEach {
                    AssistChip(onClick = { unitToggle = it }, label = { Text(it.uppercase()) })
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(onClick = {
                DatePickerDialog(context, { _, y, m, d ->
                    calendar.set(y, m, d)
                    expDate = formatter.format(calendar.time)
                }, calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH]).show()
            }, modifier = Modifier.fillMaxWidth()) {
                Text(if (expDate.isBlank()) "Pick Expiration Date" else "Exp Date: $expDate")
            }

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ActionButton(Icons.Default.CameraAlt, "Camera") { cameraLauncher.launch(null) }
                ActionButton(Icons.Default.Image, "Gallery") { galleryLauncher.launch("image/*") }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isBlank() || category.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("❗ Fill name and category.") }
                        return@Button
                    }
                    val uid = auth.currentUser?.uid ?: return@Button
                    val product = mapOf(
                        "name" to name,
                        "category" to category,
                        "quantity" to quantity.toString(),
                        "weight" to displayWeight,
                        "expDate" to expDate,
                        "addDate" to formatter.format(Date()),
                        "source" to when {
                            scannedText != null -> "ocr"
                            scannedBarcode != null -> "barcode"
                            else -> "manual"
                        }
                    )
                    db.collection("users").document(uid).collection("products")
                        .add(product)
                        .addOnSuccessListener {
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "✅ Product saved!",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.Dismissed || result == SnackbarResult.ActionPerformed) {
                                    navController.navigate("products") {
                                        popUpTo("products") { inclusive = true }
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {
                            scope.launch {
                                snackbarHostState.showSnackbar("❌ Failed to save.")
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Product")
            }
        }
    }
}

fun normalizeWeight(text: String): String {
    val match = Regex("(\\d+(?:[.,]\\d+)?)(\\s*)(g|kg|ml|l)", RegexOption.IGNORE_CASE).find(text)
    return match?.value?.trim()?.lowercase() ?: ""
}

fun formatWeight(weight: String, toggle: String): String {
    val match = Regex("(\\d+(?:[.,]\\d+)?)(\\s*)(g|kg|ml|l)", RegexOption.IGNORE_CASE).find(weight) ?: return weight
    val (valueRaw, _, unit) = match.destructured
    val value = valueRaw.replace(",", ".").toFloat()
    return when (unit.lowercase()) {
        "g" -> if (toggle == "kg" || toggle == "auto") "%.1f kg".format(value / 1000) else "${value.toInt()} g"
        "kg" -> if (toggle == "g") "${(value * 1000).toInt()} g" else "%.1f kg".format(value)
        "ml" -> if (toggle == "l" || toggle == "auto") "%.1f L".format(value / 1000) else "${value.toInt()} ml"
        "l" -> if (toggle == "ml") "${(value * 1000).toInt()} ml" else "%.1f L".format(value)
        else -> weight
    }
}

suspend fun processImage(bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    analyzeImageWithVisionAPI(base64)
}

suspend fun analyzeImageWithVisionAPI(base64Image: String): String? = withContext(Dispatchers.IO) {
    val apiKey = "AIzaSyA83ZQcpyY4fejofjZatmChBoBTbXsGghQ" // Use your actual API key
    val jsonPayload = """
    {
      "requests": [
        {
          "image": { "content": "$base64Image" },
          "features": [ { "type": "LABEL_DETECTION", "maxResults": 3 } ]
        }
      ]
    }
    """.trimIndent()
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://vision.googleapis.com/v1/images:annotate?key=$apiKey")
        .post(jsonPayload.toRequestBody("application/json".toMediaType()))
        .build()
    try {
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: return@withContext null
        val json = JSONObject(responseBody)
        json.getJSONArray("responses")
            .getJSONObject(0)
            .optJSONArray("labelAnnotations")
            ?.getJSONObject(0)
            ?.getString("description")
    } catch (e: Exception) {
        Log.e("VisionAPI", "Error: ${e.message}")
        null
    }
}

fun fetchProductInfoFromBarcode(
    context: Context,
    barcode: String,
    onResult: (name: String, category: String, weight: String) -> Unit
) {
    val url = "https://world.openfoodfacts.org/api/v0/product/$barcode.json"
    val request = Request.Builder().url(url).build()

    OkHttpClient().newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("BarcodeLookup", "Failed to fetch product info: ${e.message}")
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "❌ Network error.", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { body ->
                try {
                    val json = JSONObject(body)

                    if (json.optInt("status") == 0) {
                        // Product not found
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "❌ Product not found for barcode: $barcode", Toast.LENGTH_SHORT).show()
                        }
                        onResult("", "", "")
                        return
                    }

                    val product = json.getJSONObject("product")
                    val nameRaw = product.optString("product_name")
                    val name = if (nameRaw.isNullOrBlank()) "" else nameRaw
                    val categoryTags = product.optJSONArray("categories_tags")
                    val category = if (categoryTags != null && categoryTags.length() > 0) {
                        categoryTags.getString(0).removePrefix("en:")
                    } else "Other"

                    val quantity = product.optString("product_quantity", "")
                    val unit = product.optString("quantity_unit", "")
                    val weight = "$quantity $unit".trim()

                    CoroutineScope(Dispatchers.Main).launch {
                        onResult(name, category, weight)
                    }
                } catch (e: Exception) {
                    Log.e("BarcodeParse", "Error: ${e.message}")
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "⚠️ Parsing error.", Toast.LENGTH_SHORT).show()
                    }
                    onResult("", "", "")
                }
            }
        }
    })
}
