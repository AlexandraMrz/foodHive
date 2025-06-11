package com.example.foodhive.ui.utils

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

fun uriToTempFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("avatar", ".jpg", context.cacheDir)
        tempFile.outputStream().use { fileOut -> inputStream.copyTo(fileOut) }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun updateProfilePic(
    context: Context,
    imageUri: Uri,
    onSuccess: (String) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val storageRef = FirebaseStorage.getInstance().reference
        .child("users/$uid/avatar.jpg")

    val tempFile = uriToTempFile(context, imageUri)
    if (tempFile != null) {
        val fileUri = Uri.fromFile(tempFile)
        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    FirebaseFirestore.getInstance().collection("users")
                        .document(uid)
                        .update("avatarUrl", uri.toString())
                        .addOnSuccessListener { onSuccess(uri.toString()) }
                        .addOnFailureListener { onFailure(it) }
                }.addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    } else {
        onFailure(Exception("Failed to create temp file."))
    }
}

