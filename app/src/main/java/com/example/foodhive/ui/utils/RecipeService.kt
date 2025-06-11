package com.example.foodhive.api

import android.util.Log
import androidx.core.text.HtmlCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

const val SPOONACULAR_API_KEY = "6f4125aedc4a4aecbdce3d26a0f650e5"

data class Recipe(
    val id: Int,
    val title: String,
    val image: String,
    val ingredients: List<String>,
    val instructions: String,
    val sourceUrl: String = ""
)

fun extractExclusions(query: String): Pair<String, List<String>> {
    val lowercaseQuery = query.lowercase().trim()

    val normalized = lowercaseQuery
        .replace("i want", "")
        .replace("please", "")
        .replace("something", "")
        .replace("show me", "")
        .replace("can i have", "")
        .trim()

    val pattern = Regex("without\\s+([a-zA-Z,\\s]+)")
    val match = pattern.find(normalized)

    val exclusions = match?.groupValues?.get(1)
        ?.split(",")
        ?.map { it.trim() }
        ?: emptyList()

    val cleanedQuery = pattern.replace(normalized, "").trim()
    return Pair(cleanedQuery, exclusions)
}

suspend fun getTop3Recipes(query: String, diet: String?, exclusions: String?): List<Recipe> = withContext(Dispatchers.IO) {
    val client = OkHttpClient()

    val (cleanedQuery, extracted) = extractExclusions(query)
    val finalExclusions = (extracted + (exclusions?.split(",") ?: emptyList())).map { it.trim() }.filter { it.isNotEmpty() }

    val encodedQuery = URLEncoder.encode(cleanedQuery.ifBlank { "dinner" }, StandardCharsets.UTF_8.toString())
    val offset = listOf(0, 3, 6, 9, 12, 15, 18, 21, 24, 27, 30).random()

    val dietParam = diet?.takeIf { it != "none" }?.let {
        "&diet=" + URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
    } ?: ""

    val excludeParam = if (finalExclusions.isNotEmpty()) {
        "&excludeIngredients=" + URLEncoder.encode(finalExclusions.joinToString(","), StandardCharsets.UTF_8.toString())
    } else ""

    val url = "https://api.spoonacular.com/recipes/complexSearch" +
            "?query=$encodedQuery" +
            "$dietParam$excludeParam" +
            "&number=3&offset=$offset" +
            "&instructionsRequired=true" +
            "&addRecipeInformation=false" +
            "&apiKey=$SPOONACULAR_API_KEY"

    Log.d("RecipeService", "🧠 Query: $query")
    Log.d("RecipeService", "🔍 Cleaned: $cleanedQuery")
    Log.d("RecipeService", "🥗 Diet: $diet")
    Log.d("RecipeService", "❌ Exclusions: $finalExclusions")
    Log.d("RecipeService", "🌐 URL: $url")

    try {
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        Log.d("RecipeService", "🔁 Response Code: ${response.code}")
        if (!response.isSuccessful) return@withContext emptyList()

        val result = response.body?.string() ?: return@withContext emptyList()
        val root = JSONObject(result)
        val resultsArray = root.getJSONArray("results")

        return@withContext (0 until resultsArray.length()).mapNotNull { i ->
            val obj = resultsArray.getJSONObject(i)
            val recipeId = obj.optInt("id", -1)
            fetchFullRecipe(recipeId)
        }

    } catch (e: Exception) {
        Log.e("RecipeService", "❌ Error in getTop3Recipes", e)
        return@withContext emptyList()
    }
}

suspend fun fetchFullRecipe(id: Int): Recipe? = withContext(Dispatchers.IO) {
    val url = "https://api.spoonacular.com/recipes/$id/information?includeNutrition=false&apiKey=$SPOONACULAR_API_KEY"
    Log.d("RecipeService", "🌐 Fetching full info: $url")

    try {
        val request = Request.Builder().url(url).build()
        val response = OkHttpClient().newCall(request).execute()
        if (!response.isSuccessful) return@withContext null

        val json = JSONObject(response.body?.string() ?: return@withContext null)

        val title = json.optString("title", "Untitled")
        val image = json.optString("image", "")
        val sourceUrl = json.optString("sourceUrl")
        val ingredients = json.optJSONArray("extendedIngredients")?.let { array ->
            List(array.length()) { i -> array.getJSONObject(i).optString("name", "") }
        }?.filter { it.isNotBlank() } ?: emptyList()

        val rawInstructions = json.optString("instructions")
        val instructions = HtmlCompat.fromHtml(rawInstructions, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            .takeIf { it.isNotBlank() } ?: "No instructions provided."

        Log.d("RecipeService", "✅ Full recipe loaded: $title with ${ingredients.size} ingredients")
        return@withContext Recipe(id, title, image, ingredients, instructions, sourceUrl)

    } catch (e: Exception) {
        Log.e("RecipeService", "❌ Failed to fetch full recipe info", e)
        return@withContext null
    }
}

suspend fun generateBotReply(query: String, diet: String, exclusions: String): List<Recipe> {
    return getTop3Recipes(query, diet, exclusions)
}
