package com.example.foodhive.auth

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.foodhive.ui.components.scaffold.MainScaffold
import com.example.foodhive.ui.utils.NotificationPreferencesManager
import com.example.foodhive.ui.utils.ThemePreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

data class NotificationSettings(
    val expirationAlerts: Boolean = true,
    val dailyTip: Boolean = true
)

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val notificationManager = remember { NotificationPreferencesManager(context) }
    val themeManager = remember { ThemePreferencesManager(context) }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid

    val expirationAlerts by notificationManager.expirationAlertsFlow.collectAsState(initial = true)
    val dailyTip by notificationManager.dailyTipFlow.collectAsState(initial = true)
    val themeMode by themeManager.themeModeFlow.collectAsState(initial = AppThemeMode.SYSTEM.name)

    var diet by remember { mutableStateOf("") }
    var exclusions by remember { mutableStateOf("") }

    // Load saved preferences
    LaunchedEffect(Unit) {
        uid?.let {
            db.collection("users").document(it).get().addOnSuccessListener { doc ->
                val prefs = doc.get("preferences") as? Map<*, *>
                diet = prefs?.get("diet") as? String ?: ""
                exclusions = (prefs?.get("exclusions") as? List<*>)?.joinToString(", ") ?: ""
            }
        }
    }

    MainScaffold(navController = navController, currentScreen = "Settings") {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Settings", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))

            // 🔔 Notification settings
            NotificationSettingsSection(
                expirationEnabled = expirationAlerts,
                onExpirationToggle = {
                    coroutineScope.launch { notificationManager.setExpirationAlerts(it) }
                },
                dailyTipEnabled = dailyTip,
                onDailyTipToggle = {
                    coroutineScope.launch { notificationManager.setDailyTip(it) }
                }
            )

            Spacer(Modifier.height(32.dp))

            // 🥗 Dietary preferences
            Text("Diet Type (e.g., vegetarian, vegan)", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = diet,
                onValueChange = { diet = it },
                label = { Text("Diet") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Exclude Ingredients (comma separated)", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = exclusions,
                onValueChange = { exclusions = it },
                label = { Text("Exclusions") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                val exclusionList = exclusions.split(",").map { it.trim().lowercase() }
                val data = mapOf("preferences" to mapOf("diet" to diet, "exclusions" to exclusionList))
                uid?.let {
                    db.collection("users").document(it).update(data)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Preferences saved!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to save preferences", Toast.LENGTH_SHORT).show()
                        }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Save Preferences")
            }

            Spacer(Modifier.height(32.dp))

            // 🎨 Theme settings
            ThemeSettingsSection(
                currentTheme = AppThemeMode.valueOf(themeMode),
                onThemeSelected = {
                    coroutineScope.launch { themeManager.setThemeMode(it) }
                },
                context = context
            )
        }
    }
}

@Composable
fun NotificationSettingsSection(
    expirationEnabled: Boolean,
    onExpirationToggle: (Boolean) -> Unit,
    dailyTipEnabled: Boolean,
    onDailyTipToggle: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Notifications", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Expiration alerts", modifier = Modifier.weight(1f))
            Switch(checked = expirationEnabled, onCheckedChange = onExpirationToggle)
        }

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Daily tip of the day", modifier = Modifier.weight(1f))
            Switch(checked = dailyTipEnabled, onCheckedChange = onDailyTipToggle)
        }
    }
}

@Composable
fun ThemeSettingsSection(
    currentTheme: AppThemeMode,
    onThemeSelected: (AppThemeMode) -> Unit,
    context: Context
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("App Theme", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        AppThemeMode.values().forEach { mode ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = currentTheme == mode,
                    onClick = { onThemeSelected(mode) }
                )
                Spacer(Modifier.width(8.dp))
                Text(text = mode.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }

        Spacer(Modifier.height(32.dp))

        // 📬 Feedback Button
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("support@foodhive.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "Feedback for FoodHive App")
                    putExtra(Intent.EXTRA_TEXT, "Hi FoodHive team,\n\nHere is my feedback:\n")
                }
                val chooser = Intent.createChooser(intent, "Send Feedback")
                context.startActivity(chooser)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send Feedback 📬")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 📱 App Version Info
        val versionName = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            "Unknown"
        }

        Text(
            text = "App Version: $versionName",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
