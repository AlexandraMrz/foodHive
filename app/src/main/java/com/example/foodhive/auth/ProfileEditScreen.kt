package com.example.foodhive.ui.components.screens.auth

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.foodhive.R
import com.example.foodhive.ui.components.scaffold.MainScaffold
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

@Composable
fun ProfileImagePicker(
    avatarUrl: String?,
    onImageSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onImageSelected(it) }
    }

    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .clickable { launcher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl != null) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.default_avatar),
                contentDescription = "Default Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun ProfileEditScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val user = auth.currentUser
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var imageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }

    LaunchedEffect(Unit) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    fullName = doc.getString("fullName") ?: ""
                    phone = doc.getString("phone") ?: ""
                    imageUrl = doc.getString("profileImage") ?: ""
                    isLoading = false
                }
                .addOnFailureListener {
                    scope.launch {
                        snackbarHostState.showSnackbar("❌ Failed to load profile.")
                    }
                    isLoading = false
                }
        }
    }

    MainScaffold(navController = navController, currentScreen = "Edit Profile") {

        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val imagePainter = rememberAsyncImagePainter(
                        model = selectedImageUri ?: imageUrl.ifEmpty { R.drawable.default_avatar }
                    )
                    Image(
                        painter = imagePainter,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .clickable { launcher.launch("image/*") }
                    )

                    if (imageUrl.isNotEmpty() || selectedImageUri != null) {
                        TextButton(onClick = {
                            user?.uid?.let { uid ->
                                val path = imageUrl.substringAfter("/o/").substringBefore("?").replace("%2F", "/")
                                val ref = storage.reference.child(path)
                                ref.delete()
                                    .addOnSuccessListener {
                                        imageUrl = ""
                                        selectedImageUri = null
                                        db.collection("users").document(uid)
                                            .update("profileImage", "")
                                        scope.launch {
                                            snackbarHostState.showSnackbar("🗑 Profile image removed.")
                                        }
                                    }
                                    .addOnFailureListener {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("⚠️ Failed to remove image.")
                                        }
                                    }
                            }
                        }) {
                            Text("Remove Image")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Email: ${user?.email ?: ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (fullName.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("⚠️ Full name cannot be empty.")
                                }
                                return@Button
                            }

                            user?.uid?.let { uid ->
                                val updates = mutableMapOf<String, Any>("fullName" to fullName)
                                if (phone.isNotBlank()) updates["phone"] = phone

                                val uploadAndSave = {
                                    Log.d("ProfileEdit", "Saving profile for UID: $uid")
                                    Log.d("ProfileEdit", "Updates: $updates")
                                    db.collection("users").document(uid)
                                        .set(updates, SetOptions.merge())
                                        .addOnSuccessListener {
                                            Log.d("ProfileEdit", "Profile updated successfully")
                                            scope.launch {
                                                snackbarHostState.showSnackbar("✅ Profile updated!")
                                            }
                                            navController.popBackStack()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("ProfileEdit", "Failed to update Firestore", e)
                                            scope.launch {
                                                snackbarHostState.showSnackbar("❌ Failed to update profile: ${e.message}")
                                            }
                                        }
                                }

                                selectedImageUri?.let { uri ->
                                    val filename = "profile_images/${uid}_${System.currentTimeMillis()}.jpg"
                                    val ref = storage.reference.child(filename)

                                    ref.putFile(uri)
                                        .continueWithTask { task: Task<*> ->
                                            if (!task.isSuccessful) {
                                                task.exception?.let { throw it }
                                            }
                                            ref.downloadUrl
                                        }
                                        .addOnSuccessListener { downloadUri ->
                                            updates["profileImage"] = downloadUri.toString()
                                            uploadAndSave()
                                        }
                                        .addOnFailureListener {
                                            Log.e("ProfileEdit", "Image upload failed", it)
                                            scope.launch {
                                                snackbarHostState.showSnackbar("❌ Image upload failed.")
                                            }
                                        }
                                } ?: uploadAndSave()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
