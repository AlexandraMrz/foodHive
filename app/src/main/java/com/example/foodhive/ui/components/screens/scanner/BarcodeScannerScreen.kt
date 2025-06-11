@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.foodhive.ui.components.screens.scanner

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Size
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.foodhive.Screen
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.Executors

@Composable
fun BarcodeScannerScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }

    val scanned = remember { mutableStateOf(false) }
    val isScanning = remember { mutableStateOf(true) }
    var permissionDenied by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val activity = context as Activity
        val permission = Manifest.permission.CAMERA

        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), 0)
            permissionDenied = true
            return@LaunchedEffect
        }

        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        val scanner = BarcodeScanning.getClient(options)

        val analysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1920, 1080))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
            if (!scanned.value) {
                processImageProxy(scanner, imageProxy, navController) {
                    scanned.value = false
                    isScanning.value = true
                }
                scanned.value = true
                isScanning.value = false
            } else {
                imageProxy.close()
            }
        }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
    }

    Box(Modifier.fillMaxSize()) {
        if (permissionDenied) {
            Text("Camera permission denied.", Modifier.align(Alignment.Center))
        } else {
            AndroidView({ previewView }, Modifier.fillMaxSize())
            if (isScanning.value) {
                Column(Modifier.align(Alignment.BottomCenter).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text("Scanning for barcode...")
                }
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalGetImage::class)
fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    navController: NavController,
    onResetScan: () -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val barcode = barcodes.firstOrNull()
                val value = barcode?.rawValue
                if (value.isNullOrBlank()) {
                    Toast.makeText(navController.context, "Invalid barcode.", Toast.LENGTH_SHORT).show()
                    onResetScan()
                    imageProxy.close()
                } else {
                    fetchProductFromOpenFoodFacts(value, navController) {
                        onResetScan()
                        imageProxy.close()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(navController.context, "Scan error.", Toast.LENGTH_SHORT).show()
                onResetScan()
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

fun fetchProductFromOpenFoodFacts(barcode: String?, navController: NavController, onComplete: () -> Unit) {
    if (barcode.isNullOrBlank()) {
        Toast.makeText(navController.context, "Invalid barcode.", Toast.LENGTH_SHORT).show()
        onComplete()
        return
    }

    val client = OkHttpClient()
    val url = "https://world.openfoodfacts.org/api/v0/product/$barcode.json"
    val request = Request.Builder().url(url).build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(navController.context, "Failed to fetch product.", Toast.LENGTH_SHORT).show()
                onComplete()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val json = response.body?.string()
            val obj = JSONObject(json ?: "")
            val status = obj.optInt("status", 0)

            Handler(Looper.getMainLooper()).post {
                if (status == 1) {
                    navController.navigate(Screen.AddProduct.withBarcode(barcode))
                } else {
                    Toast.makeText(navController.context, "Product not found.", Toast.LENGTH_LONG).show()
                }
                onComplete()
            }
        }
    })
}
