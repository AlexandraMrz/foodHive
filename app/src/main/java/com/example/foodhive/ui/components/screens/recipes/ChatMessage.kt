package com.example.foodhive.ui.components.screens.recipes

import com.google.firebase.firestore.PropertyName

data class ChatMessage(
    @get:PropertyName("text") @set:PropertyName("text")
    var text: String = "",

    @get:PropertyName("user") @set:PropertyName("user")
    var isUser: Boolean = false,

    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var timestamp: Long = 0L
)