@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.foodhive.ui.components.recipes
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.foodhive.api.Recipe
import com.example.foodhive.ui.components.screens.recipes.MessageBubble
import com.example.foodhive.ui.components.screens.recipes.ChatItem
import com.example.foodhive.ui.components.screens.recipes.ChatMeta
import com.example.foodhive.ui.components.recipes.RecipeChatBubble


@Composable
fun RecipeBotLayout(
    userId: String,
    chatId: String,
    allChats: List<ChatMeta>,
    messages: List<ChatItem>,
    userInput: String,
    onUserInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onLaunchVoiceInput: () -> Unit,
    onNewChat: () -> Unit,
    onSelectChat: (String) -> Unit,
    onRenameChat: (ChatMeta) -> Unit,
    onDeleteChat: (ChatMeta) -> Unit,
    onAddMissingIngredients: (Recipe) -> Unit,
    onAddToFavorites: (Recipe) -> Unit,
    showSidebar: Boolean,
    toggleSidebar: () -> Unit,
    listState: LazyListState,
    context: Context,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("RecipeBot 🤖") },
                actions = {
                    IconButton(onClick = toggleSidebar) {
                        Icon(Icons.Filled.Mic, contentDescription = "Voice Input")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                state = listState
            ) {
                items(messages.size) { index ->
                    when (val item = messages[index]) {
                        is ChatItem.Text -> MessageBubble(item.message)
                        is ChatItem.RecipeEntry -> RecipeChatBubble(
                            recipe = item.recipe,
                            onAddToFavorites = { onAddToFavorites(item.recipe) },
                            onAddMissingIngredients = { onAddMissingIngredients(item.recipe) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = userInput,
                    onValueChange = onUserInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask me for a recipe...") },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSendMessage() })
                )
                IconButton(onClick = onSendMessage) {
                    Icon(Icons.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}
