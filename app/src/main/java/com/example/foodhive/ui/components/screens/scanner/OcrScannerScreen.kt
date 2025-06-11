@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalGetImage::class)

package com.example.foodhive.ui.components.screens.scanner

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun OcrScannerScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as Activity
    val lifecycleOwner = activity as LifecycleOwner
    val previewView = remember { PreviewView(context) }

    var cameraExecutor: ExecutorService? by remember { mutableStateOf(null) }
    var detectedText by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }
    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        if (granted) isScanning = true
    }

    DisposableEffect(Unit) {
        cameraExecutor = Executors.newSingleThreadExecutor()
        onDispose {
            cameraExecutor?.shutdown()
        }
    }

    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            val cameraProvider = ProcessCameraProvider.getInstance(context).get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            val analysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(cameraExecutor!!, { imageProxy ->
                if (isScanning) {
                    imageProxy.image?.let { mediaImage ->
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        recognizer.process(image)
                            .addOnSuccessListener { result ->
                                val text = result.textBlocks.joinToString("\n") { it.text }
                                if (text.isNotBlank()) {
                                    detectedText = text
                                    isScanning = false
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("OCR", "OCR failed: ${e.message}")
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } ?: imageProxy.close()
                } else {
                    imageProxy.close()
                }
            })

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, analysis)
            cameraInstance = camera
        }
    }

    // Initial permission check
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            permissionGranted = true
            isScanning = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // UI
    Box(modifier = Modifier.fillMaxSize()) {
        if (!permissionGranted) {
            Text(
                text = "Camera permission required.",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AndroidView(factory = { previewView }, modifier = Modifier.weight(1f))

                if (detectedText.isBlank()) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                } else {
                    Text(
                        text = "Detected:\n$detectedText",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = {
                        val firstLine = detectedText.lines().firstOrNull()?.trim()
                        if (!firstLine.isNullOrBlank()) {
                            val encoded = Uri.encode(firstLine)
                            navController.navigate("addProduct/$encoded")
                        } else {
                            Toast.makeText(context, "No text detected!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Use Detected Text")
                }

                TextButton(
                    onClick = { isScanning = true },
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text("Scan Again")
                }

                TextButton(
                    onClick = {
                        isTorchOn = !isTorchOn
                        cameraInstance?.cameraControl?.enableTorch(isTorchOn)
                    },
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(if (isTorchOn) "Turn Off Flashlight" else "Turn On Flashlight")
                }

                TextButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
