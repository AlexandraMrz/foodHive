package com.example.foodhive.ui.components.recipes

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import coil.compose.rememberAsyncImagePainter
import com.example.foodhive.api.Recipe

@Composable
fun RecipeChatBubble(
    recipe: Recipe,
    onAddToFavorites: (() -> Unit)? = null,
    onAddMissingIngredients: (() -> Unit)? = null
) {
    val context = LocalContext.current

    val bubbleGradient = Brush.horizontalGradient(
        listOf(Color(0xFFF5EBE0), Color(0xFFDDB892))
    )

    val cleanInstructions = HtmlCompat.fromHtml(recipe.instructions, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    val textColor = Color.Black

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(brush = bubbleGradient, shape = RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleMedium,
                color = textColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Image(
                painter = rememberAsyncImagePainter(recipe.image),
                contentDescription = "Recipe image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("🧂 Ingredients:", style = MaterialTheme.typography.titleSmall, color = textColor)
            Text(
                text = recipe.ingredients.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("📖 Instructions:", style = MaterialTheme.typography.titleSmall, color = textColor)
            Text(
                text = cleanInstructions,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )

            if (!recipe.sourceUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🔗 View full recipe",
                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(recipe.sourceUrl))
                            context.startActivity(intent)
                        }
                        .padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (onAddToFavorites != null) {
                    Button(onClick = onAddToFavorites) {
                        Text("❤️ Add to Favorites")
                    }
                }
                if (onAddMissingIngredients != null) {
                    FilledTonalButton(onClick = onAddMissingIngredients) {
                        Text("🛒 Add Missing Items")
                    }
                }
            }
        }
    }
}
