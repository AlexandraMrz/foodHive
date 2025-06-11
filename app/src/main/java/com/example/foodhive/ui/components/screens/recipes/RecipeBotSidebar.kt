package com.example.foodhive.ui.components.screens.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@Composable
fun RecipeBotSidebar(
    chatId: String,
    allChats: List<ChatMeta>,
    onSelectChat: (String) -> Unit,
    onRenameChat: (ChatMeta) -> Unit,
    onDeleteChat: (ChatMeta) -> Unit,
    onNewChat: () -> Unit
) {
    val sidebarGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFF5EBE0), Color(0xFFDDB892))
    )

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(brush = sidebarGradient)
            .zIndex(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                "🕘 Chat History",
                fontSize = 20.sp,
                color = Color(0xFF3E2723),
                style = MaterialTheme.typography.titleMedium
            )

            Divider(
                Modifier.padding(vertical = 8.dp),
                color = Color(0xFF795548)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(allChats) { chat ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = if (chat.id == chatId) 4.dp else 0.dp,
                        color = if (chat.id == chatId) Color(0xFFEFEBE9) else Color(0xFFFBE9E7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelectChat(chat.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = chat.name ?: chat.id,
                                modifier = Modifier.weight(1f),
                                fontSize = 16.sp,
                                color = Color(0xFF3E2723),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(onClick = {
                                println("📝 Rename clicked: ${chat.id}")
                                onRenameChat(chat)
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename", tint = Color(0xFF6D4C41))
                            }
                            IconButton(onClick = {
                                println("❌ Delete clicked: ${chat.id}")
                                onDeleteChat(chat)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF6D4C41))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    println("➕ New chat clicked")
                    onNewChat()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("➕ New Chat", fontSize = 16.sp)
            }
        }
    }
}
