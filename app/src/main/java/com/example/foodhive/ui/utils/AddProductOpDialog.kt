// AddProductOptionDialog.kt

package com.example.foodhive.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddProductOptionDialog(
    onManualEntry: () -> Unit,
    onScanEntry: () -> Unit,
    onOcrEntry: () -> Unit,
    onAiImageEntry: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Product") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onManualEntry,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Manually")
                }
                Button(
                    onClick = onScanEntry,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Scan Barcode")
                }
                Button(
                    onClick = onOcrEntry,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Text Recognition (OCR)")
                }
                Button(onAiImageEntry, modifier = Modifier.fillMaxWidth()) {
                    Text("Image Recognition")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}