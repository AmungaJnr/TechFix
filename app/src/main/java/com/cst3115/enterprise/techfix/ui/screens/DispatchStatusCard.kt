package com.cst3115.enterprise.techfix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cst3115.enterprise.techfix.ui.viewmodel.DispatchUiState

/**
 * Displays the demo network action, progress, and latest result.
 */
@Composable
fun DispatchStatusCard(
    state: DispatchUiState,
    enabled: Boolean,
    onSend: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Demo dispatch update",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Send today's assigned, pending, and completed totals. " +
                        "Client details and resolution notes stay on this device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (state.isSending) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Sending workload snapshot...",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            state.message?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            OutlinedButton(
                onClick = onSend,
                enabled = enabled && !state.isSending,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when {
                        state.isSending -> "Sending..."
                        state.isError -> "Retry demo update"
                        else -> "Send demo update"
                    }
                )
            }
        }
    }
}