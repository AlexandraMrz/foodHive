import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.foodhive.api.Recipe
import com.example.foodhive.ui.components.screens.recipes.ChatMessage
import com.example.foodhive.ui.components.screens.shopping.ShoppingListRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun rememberVoiceLauncher(onResult: (String) -> Unit): ActivityResultLauncher<Intent> {
    val context = LocalContext.current
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            spokenText?.let { onResult(it) }
        }
    }
}

fun launchVoiceInput(context: Context, launcher: ActivityResultLauncher<Intent>) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your recipe request")
    }
    launcher.launch(intent)
}

fun saveMessage(userId: String, chatId: String, message: ChatMessage) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users")
        .document(userId)
        .collection("chats")
        .document(chatId)
        .collection("messages")
        .add(
            mapOf(
                "text" to message.text,
                "user" to message.isUser,
                "timestamp" to message.timestamp
            )
        )
}

fun saveRecipe(userId: String, chatId: String, messageId: String, recipe: Recipe) {
    val db = FirebaseFirestore.getInstance()
    val recipeRef = db.collection("users")
        .document(userId)
        .collection("chats")
        .document(chatId)
        .collection("messages")
        .document(messageId)
        .collection("recipe")
        .document(recipe.id.toString())

    val recipeData = mapOf(
        "title" to recipe.title,
        "image" to recipe.image,
        "ingredients" to recipe.ingredients,
        "instructions" to recipe.instructions
    )

    recipeRef.set(recipeData)
        .addOnSuccessListener {
            println("✅ Recipe saved under messages/$messageId/recipe.")
        }
        .addOnFailureListener { e ->
            println("❌ Failed to save recipe: ${e.message}")
        }
}

fun addMissingIngredientsToShoppingList(userId: String, recipe: Recipe) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val missing = ShoppingListRepository.getMissingIngredients(userId, recipe.ingredients)
            if (missing.isNotEmpty()) {
                ShoppingListRepository.addMissingItems(userId, missing)
                println("✅ Added ${missing.size} missing ingredients to shopping list.")
            } else {
                println("✅ No missing ingredients to add.")
            }
        } catch (e: Exception) {
            println("❌ Error adding missing ingredients: ${e.message}")
        }
    }
}

fun saveFavoriteRecipe(userId: String, recipe: Recipe) {
    val db = FirebaseFirestore.getInstance()
    val docRef = db.collection("users").document(userId).collection("favorites").document(recipe.id.toString())

    val data = mapOf(
        "id" to recipe.id,
        "title" to recipe.title,
        "image" to recipe.image,
        "ingredients" to recipe.ingredients,
        "instructions" to recipe.instructions,
        "sourceUrl" to recipe.sourceUrl,
        "timestamp" to System.currentTimeMillis()
    )

    docRef.set(data)
        .addOnSuccessListener { println("✅ Saved favorite recipe.") }
        .addOnFailureListener { e -> println("❌ Failed to save favorite: ${e.message}") }
}
