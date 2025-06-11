package com.example.foodhive.ui.components.screens.product

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.foodhive.ui.components.scaffold.MainScaffold
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await



@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FavScreen(navController: NavController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val favourites = remember { mutableStateListOf<FavouriteItem>() }
    val searchQuery = remember { mutableStateOf(TextFieldValue("")) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedItem by remember { mutableStateOf<FavouriteItem?>(null) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(userId) {
        userId?.let {
            val fetched = FavouritesRepository.fetchFavourites(it)
            favourites.clear()
            favourites.addAll(fetched)
        }
    }

    val filteredList = remember(searchQuery.value, favourites) {
        if (searchQuery.value.text.isBlank()) favourites
        else favourites.filter {
            it.title.contains(searchQuery.value.text, ignoreCase = true)
        }
    }

    MainScaffold(navController = navController, currentScreen = "Favourites") {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)) {

                // 🔍 Search Bar
                OutlinedTextField(
                    value = searchQuery.value,
                    onValueChange = { searchQuery.value = it },
                    label = { Text("Search favorites...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No favourites match your search.")
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filteredList, key = { it.id }) { recipe ->
                            val dismissState = rememberDismissState(
                                confirmStateChange = {
                                    if (it == DismissValue.DismissedToStart) {
                                        coroutineScope.launch {
                                            userId?.let { uid ->
                                                FirebaseFirestore.getInstance()
                                                    .collection("users")
                                                    .document(uid)
                                                    .collection("favorites")
                                                    .document(recipe.id.toString())
                                                    .delete().await()

                                                val updated = FavouritesRepository.fetchFavourites(uid)
                                                favourites.clear()
                                                favourites.addAll(updated)

                                                snackbarHostState.showSnackbar(
                                                    "\"${recipe.title}\" removed from favourites."
                                                )
                                            }
                                        }
                                    }
                                    true
                                }
                            )

                            SwipeToDismiss(
                                state = dismissState,
                                directions = setOf(DismissDirection.EndToStart),
                                background = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Red)
                                            .padding(20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.White
                                        )
                                    }
                                },
                                dismissContent = {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedItem = recipe },
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(12.dp)
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Image(
                                                painter = rememberAsyncImagePainter(recipe.image),
                                                contentDescription = "Recipe image",
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(recipe.title, style = MaterialTheme.typography.titleMedium)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // ✨ Animated Modal (Bottom Sheet)
            if (selectedItem != null) {
                ModalBottomSheet(
                    onDismissRequest = { selectedItem = null },
                    sheetState = bottomSheetState
                ) {
                    selectedItem?.let { recipe ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(recipe.title, style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("🧂 Ingredients", style = MaterialTheme.typography.titleSmall)
                            Text(recipe.ingredients.joinToString(", ").ifBlank { "No ingredients available." })

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("📋 Instructions", style = MaterialTheme.typography.titleSmall)
                            Text(recipe.instructions.ifBlank { "No instructions provided." })

                            if (recipe.sourceUrl.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                TextButton(onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(recipe.sourceUrl))
                                    navController.context.startActivity(intent)
                                }) {
                                    Text("🌐 View Full Recipe")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = { selectedItem = null }) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }
}
