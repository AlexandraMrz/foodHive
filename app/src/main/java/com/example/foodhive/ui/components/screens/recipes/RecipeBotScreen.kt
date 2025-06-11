@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.foodhive.ui.components.screens.recipes

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.foodhive.api.Recipe
import com.example.foodhive.api.generateBotReply
import com.example.foodhive.ui.components.recipes.RecipeBotLayout
import com.example.foodhive.ui.components.scaffold.MainScaffold
import com.example.foodhive.ui.components.screens.shopping.ShoppingListRepository
import com.example.foodhive.ui.components.screens.shopping.ShoppingListRepository.getMissingIngredients
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import launchVoiceInput
import rememberVoiceLauncher
import saveFavoriteRecipe
import saveMessage
import saveRecipe
import java.util.UUID

@Composable
fun RecipeBotScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid.orEmpty()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    var selectedDiet by remember { mutableStateOf("none") }
    var exclusions by remember { mutableStateOf("") }

    var userInput by remember { mutableStateOf("") }
    val voiceLauncher = rememberVoiceLauncher { recognizedText -> userInput = recognizedText }
    var chatId by remember { mutableStateOf("default") }
    var allChats by remember { mutableStateOf(listOf(ChatMeta("default", "Default"))) }
    val chatItems = remember { mutableStateListOf<ChatItem>() }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<ChatMeta?>(null) }
    var renameText by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf<ChatMeta?>(null) }
    var showSidebar by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                val prefs = doc.get("preferences") as? Map<*, *>
                selectedDiet = prefs?.get("diet") as? String ?: "none"
                exclusions = (prefs?.get("exclusions") as? List<*>)?.joinToString(", ") ?: ""
            }
        }
    }

    LaunchedEffect(userId) {
        db.collection("users").document(userId).collection("chats")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    val chatList = snapshot.documents.map {
                        ChatMeta(it.id, it.getString("name") ?: it.id)
                    }
                    allChats = chatList.ifEmpty { listOf(ChatMeta("default", "Default")) }
                    if (chatList.none { it.id == chatId }) chatId = chatList.firstOrNull()?.id ?: "default"
                }
            }
    }

    LaunchedEffect(chatId) {
        db.collection("users").document(userId)
            .collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                chatItems.clear()
                snapshot?.documents?.forEach { doc ->
                    val type = doc.getString("type") ?: "text"
                    if (type == "recipe") {
                        val id = doc.getLong("id")?.toInt() ?: 0
                        val title = doc.getString("title") ?: ""
                        val image = doc.getString("image") ?: ""
                        val ingredients = doc.get("ingredients")?.toString()?.split(",")?.map { it.trim() } ?: emptyList()
                        val instructions = doc.getString("instructions") ?: ""
                        val sourceUrl = doc.getString("sourceUrl") ?: ""
                        chatItems.add(ChatItem.RecipeEntry(Recipe(id, title, image, ingredients, instructions, sourceUrl)))
                    } else {
                        val msg = doc.toObject(ChatMessage::class.java)
                        if (msg != null) chatItems.add(ChatItem.Text(msg))
                    }
                }
                if (chatItems.isEmpty()) {
                    val welcome = ChatMessage("Hi! I'm your recipe bot. Ask me anything!", false, System.currentTimeMillis())
                    chatItems.add(ChatItem.Text(welcome))
                    saveMessage(userId, chatId, welcome)
                }
            }
    }

    LaunchedEffect(chatItems.size) {
        if (chatItems.isNotEmpty()) {
            listState.animateScrollToItem(chatItems.lastIndex)
        }
    }

    MainScaffold(
        currentScreen = "RecipeBot",
        navController = navController
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(showSidebar) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (dragAmount > 30 && !showSidebar) showSidebar = true
                        if (dragAmount < -30 && showSidebar) showSidebar = false
                    }
                }
        ) {
            if (showSidebar) {
                RecipeBotSidebar(
                    chatId = chatId,
                    allChats = allChats,
                    onSelectChat = {
                        chatId = it; showSidebar = false
                    },
                    onRenameChat = {
                        renameTarget = it
                        renameText = it.name ?: it.id
                        showRenameDialog = true
                    },
                    onDeleteChat = { confirmDelete = it },
                    onNewChat = {
                        val newId = UUID.randomUUID().toString().take(6)
                        db.collection("users").document(userId).collection("chats")
                            .document(newId).set(mapOf("name" to "Chat $newId"))
                        chatId = newId
                        showSidebar = false
                    }
                )
            } else {
                RecipeBotLayout(
                    userId = userId,
                    chatId = chatId,
                    allChats = allChats,
                    messages = chatItems,
                    userInput = userInput,
                    onUserInputChange = { userInput = it },
                    onSendMessage = {
                        if (userInput.isNotBlank()) {
                            val msg = ChatMessage(userInput, true, System.currentTimeMillis())
                            chatItems.add(ChatItem.Text(msg))
                            saveMessage(userId, chatId, msg)
                            val query = userInput
                            userInput = ""
                            coroutineScope.launch {
                                val typing = ChatMessage("Typing...", false, System.currentTimeMillis())
                                chatItems.add(ChatItem.Text(typing))
                                kotlinx.coroutines.delay(1000)
                                chatItems.remove(ChatItem.Text(typing))
                                try {
                                    val recipes = generateBotReply(query, selectedDiet, exclusions)
                                    recipes.forEach { recipe ->
                                        val docRef = db.collection("users").document(userId)
                                            .collection("chats").document(chatId)
                                            .collection("messages").document()
                                        val meta = mapOf(
                                            "type" to "recipe",
                                            "timestamp" to System.currentTimeMillis(),
                                            "id" to recipe.id,
                                            "title" to recipe.title,
                                            "image" to recipe.image,
                                            "ingredients" to recipe.ingredients.joinToString(", "),
                                            "instructions" to recipe.instructions,
                                            "sourceUrl" to recipe.sourceUrl
                                        )
                                        docRef.set(meta)
                                        saveRecipe(userId, chatId, docRef.id, recipe)
                                        chatItems.add(ChatItem.RecipeEntry(recipe))
                                    }
                                } catch (e: Exception) {
                                    val error = ChatMessage("Sorry, an error occurred fetching recipes.", false, System.currentTimeMillis())
                                    chatItems.add(ChatItem.Text(error))
                                    saveMessage(userId, chatId, error)
                                }
                            }
                        }
                    },
                    onLaunchVoiceInput = { launchVoiceInput(context, voiceLauncher) },
                    onNewChat = {
                        val newId = UUID.randomUUID().toString().take(6)
                        db.collection("users").document(userId).collection("chats")
                            .document(newId).set(mapOf("name" to "Chat $newId"))
                        chatId = newId
                    },
                    onSelectChat = { chatId = it; showSidebar = false },
                    onRenameChat = {
                        renameTarget = it
                        renameText = it.name ?: it.id
                        showRenameDialog = true
                    },
                    onDeleteChat = { confirmDelete = it },
                    onAddMissingIngredients = { recipe ->
                        coroutineScope.launch {
                            val missing = getMissingIngredients(userId, recipe.ingredients)
                            val message = if (missing.isNotEmpty()) {
                                ShoppingListRepository.addMissingItems(userId, missing)
                                "📞 Added ${missing.size} missing item(s) to your shopping list!"
                            } else {
                                "✅ You already have all the ingredients for this recipe!"
                            }
                            val msg = ChatMessage(message, false, System.currentTimeMillis())
                            chatItems.add(ChatItem.Text(msg))
                            saveMessage(userId, chatId, msg)
                        }
                    },
                    onAddToFavorites = { recipe ->
                        saveFavoriteRecipe(userId, recipe)
                        val msg = ChatMessage("❤️ Added to favorites: ${recipe.title}", false, System.currentTimeMillis())
                        chatItems.add(ChatItem.Text(msg))
                        saveMessage(userId, chatId, msg)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("✅ Saved to favorites: ${recipe.title}")
                        }
                    },
                    showSidebar = showSidebar,
                    toggleSidebar = { showSidebar = !showSidebar },
                    listState = listState,
                    context = context,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}
