package com.example.foodhive.ui.components.screens.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.isUser
    val avatarText = if (isUser) "🧑" else "🤖"

    val backgroundColor: Color = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.inverseSurface
    }

    val textColor: Color = if (isUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.inverseOnSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isUser) {
            Text(
                text = avatarText,
                modifier = Modifier.padding(end = 4.dp),
                fontSize = 12.sp,
                color = textColor
            )
        }

        Box(
            modifier = Modifier
                .background(color = backgroundColor, shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 16.sp,
                color = textColor,
                lineHeight = 20.sp
            )
        }

        if (isUser) {
            Text(
                text = avatarText,
                modifier = Modifier.padding(start = 4.dp),
                fontSize = 12.sp,
                color = textColor
            )
        }
    }
}
