package com.example.foodhive.ui.components.screens.scanner

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import com.example.foodhive.ui.utils.mapToAppCategory


@Composable
fun ProductImageRecognitionSection(
    context: Context,
    nameState: MutableState<String>,
    categoryState: MutableState<String>,
    noteState: MutableState<String>,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            val base64 = encodeImageToBase64(context, uri)
            base64?.let { encoded ->
                isLoading = true
                analyzeImageWithVisionAPI(encoded, onSuccess = { label ->
                    nameState.value = label.replaceFirstChar { it.uppercase() }
                    categoryState.value = mapToAppCategory(label)
                    noteState.value = "Detected: $label"
                    isLoading = false
                    scope.launch {
                        snackbarHostState.showSnackbar("📸 Detected: $label")
                    }
                }, onFailure = {
                    isLoading = false
                    scope.launch {
                        snackbarHostState.showSnackbar("❌ Could not detect anything. Please try another photo.")
                    }
                })
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("📷 Scan Product Image")
        }

        if (imageUri != null) {
            Spacer(modifier = Modifier.height(8.dp))
            AsyncImage(
                model = imageUri,
                contentDescription = "Selected product image",
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

fun encodeImageToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun analyzeImageWithVisionAPI(
    base64Image: String,
    onSuccess: (String) -> Unit,
    onFailure: () -> Unit
) {
    val apiKey = "AIzaSyA83ZQcpyY4fejofjZatmChBoBTbXsGghQ"
    val jsonPayload = """
        {
          "requests": [{
            "image": { "content": "$base64Image" },
            "features": [{ "type": "LABEL_DETECTION", "maxResults": 3 }]
          }]
        }
    """.trimIndent()

    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://vision.googleapis.com/v1/images:annotate?key=$apiKey")
        .post(jsonPayload.toRequestBody("application/json".toMediaType()))
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            Handler(Looper.getMainLooper()).post { onFailure() }
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { body ->
                try {
                    val responseJson = JSONObject(body)
                    val annotations = responseJson
                        .getJSONArray("responses")
                        .getJSONObject(0)
                        .optJSONArray("labelAnnotations")

                    if (annotations == null || annotations.length() == 0) {
                        Handler(Looper.getMainLooper()).post { onFailure() }
                        return
                    }

                    val label = annotations.getJSONObject(0).getString("description")
                    Handler(Looper.getMainLooper()).post { onSuccess(label) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Handler(Looper.getMainLooper()).post { onFailure() }
                }
            } ?: Handler(Looper.getMainLooper()).post { onFailure() }
        }
    })
}
