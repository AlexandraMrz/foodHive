package com.example.foodhive.ui.components.screens.recipes

import com.example.foodhive.api.Recipe
import com.example.foodhive.api.generateBotReply


sealed class ChatItem {
    data class Text(val message: ChatMessage) : ChatItem()
    data class RecipeEntry(val recipe: Recipe) : ChatItem()
}
