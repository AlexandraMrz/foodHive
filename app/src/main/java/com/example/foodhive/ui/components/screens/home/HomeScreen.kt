package com.example.foodhive.ui.components.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.foodhive.ui.components.dialogs.AddProductOptionDialog
import com.example.foodhive.ui.components.scaffold.MainScaffold
import com.example.foodhive.ui.components.screens.product.ProductModel
import com.example.foodhive.ui.theme.LocalSpacing
import com.example.foodhive.ui.utils.scheduleNotificationWorkers
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    val spacing = LocalSpacing.current
    val productList by viewModel.productList.collectAsState(initial = emptyList())
    val today = LocalDate.now()
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var fullName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                fullName = doc.getString("fullName")
                scheduleNotificationWorkers(context = context, userId = uid)
            }
        }
    }

    val userName = when {
        !fullName.isNullOrBlank() -> fullName!!.split(" ").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "User"
        !user?.displayName.isNullOrBlank() -> user.displayName?.split(" ")?.firstOrNull() ?: "User"
        !user?.email.isNullOrBlank() -> user.email?.substringBefore("@") ?: "User"
        else -> "User"
    }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val expiringSoonList = productList.filter {
        runCatching {
            val expDate = LocalDate.parse(it.getExpDate(), formatter)
            val days = ChronoUnit.DAYS.between(today, expDate)
            days in 0..3
        }.getOrDefault(false)
    }

    val expiredList = productList.filter {
        runCatching {
            val expDate = LocalDate.parse(it.getExpDate(), formatter)
            expDate.isBefore(today)
        }.getOrDefault(false)
    }

    val total = productList.size
    val consumed = total - expiredList.size - expiringSoonList.size

    val categoryCount = productList.groupBy { it.getCategory() }.mapValues { it.value.size }.toList().sortedByDescending { it.second }

    val tips = listOf(
        "Plan meals ahead to reduce waste.",
        "Check expiry dates weekly!",
        "Store perishables correctly.",
        "Freeze food to extend shelf life."
    )
    val dailyTip = tips[(today.dayOfYear) % tips.size]

    MainScaffold(navController = navController, currentScreen = "Home") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.medium, vertical = spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.large)
        ) {
            WelcomeCard(userName = userName, date = today)
            TipCard(dailyTip = dailyTip)

            Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
                Text("Quick Access", style = MaterialTheme.typography.titleLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.medium)) {
                    item { OverviewCard("Products", "$total", Icons.Default.Inventory2) { navController.navigate("products") } }
                    item { OverviewCard("Expiring", "${expiringSoonList.size}", Icons.Default.WarningAmber) { navController.navigate("products?filter=expiring") } }
                    item { OverviewCard("Expired", "${expiredList.size}", Icons.Default.ErrorOutline) { navController.navigate("products?filter=expired") } }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
                Text("Top Categories", style = MaterialTheme.typography.titleLarge)
                categoryCount.take(5).forEach { (category, count) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = category ?: "Uncategorized", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "$count items", style = MaterialTheme.typography.labelMedium)
                    }
                    LinearProgressIndicator(
                        progress = count / total.toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (expiringSoonList.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                    Text("Use Soon", style = MaterialTheme.typography.titleLarge)
                    expiringSoonList.sortedBy {
                        runCatching { LocalDate.parse(it.getExpDate(), formatter) }.getOrDefault(LocalDate.MAX)
                    }.forEach { product ->
                        SuggestionCard(product = product, formatter = formatter)
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.extraLarge))
        }

        AddProductFABWithDialog(navController)
    }
}

@Composable
fun WelcomeCard(userName: String, date: LocalDate) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("👋 Welcome back,", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Text(userName, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                date.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy")),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun TipCard(dailyTip: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("💡 Tip of the Day", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Text(dailyTip, style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic))
        }
    }
}

@Composable
fun OverviewCard(title: String, count: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .size(width = 180.dp, height = 130.dp)
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = title)
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(count, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}
@Composable
fun SegmentedInventoryChart(expired: Int, expiringSoon: Int, safe: Int) {
    val barHeight = 28.dp
    val total = expired + expiringSoon + safe
    val max = if (total > 0) total.toFloat() else 1f
    val spacing = 8.dp

    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (expired > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(expired / max)
                        .background(MaterialTheme.colorScheme.error),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "$expired", color = MaterialTheme.colorScheme.onError, style = MaterialTheme.typography.labelSmall)
                }
            }
            if (expiringSoon > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(expiringSoon / max)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "$expiringSoon", color = MaterialTheme.colorScheme.onSecondary, style = MaterialTheme.typography.labelSmall)
                }
            }
            if (safe > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(safe / max)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "$safe", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Text(text = "Total: $total", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SuggestionCard(product: ProductModel, formatter: DateTimeFormatter) {
    val expDate = runCatching { LocalDate.parse(product.getExpDate(), formatter) }.getOrNull()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(product.getName().toString(), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(product.getCategory().toString(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            }
            expDate?.let {
                Text(
                    "Exp: ${it.format(DateTimeFormatter.ofPattern("d MMM"))}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddProductFABWithDialog(navController: NavController) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.End
        ) {
            FloatingActionButton(
                onClick = { navController.navigate("recipeBot") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Face, contentDescription = "RecipeBot")
            }
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
        if (showAddDialog) {
            AddProductOptionDialog(
                onManualEntry = {
                    showAddDialog = false
                    navController.navigate("addProduct")
                },
                onScanEntry = {
                    showAddDialog = false
                    navController.navigate("barcodeScanner")
                },
                onOcrEntry = {
                    showAddDialog = false
                    navController.navigate("ocrScanner")
                },
                onAiImageEntry = {
                    showAddDialog = false
                    navController.navigate("addProduct?mode=image")
                },
                onDismiss = { showAddDialog = false }
            )
        }
    }
}

