package com.cst3115.enterprise.techfix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cst3115.enterprise.techfix.Data.local.TaskEntity

/**
 * Collects and validates a new service task.
 * The parent handles persistence through the ViewModel.
 */
@Composable
fun AddTaskScreen(
    scheduledDate: String,
    isSaving: Boolean,
    errorMessage: String?,
    onDismissError: () -> Unit,
    onSaveTask: (TaskEntity) -> Unit
) {
    var clientName by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var jobDescription by rememberSaveable { mutableStateOf("") }
    var latitudeText by rememberSaveable { mutableStateOf("") }
    var longitudeText by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf("Normal") }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val latitude = latitudeText.trim().toDoubleOrNull()
    val longitude = longitudeText.trim().toDoubleOrNull()

    val latitudeValid = latitude != null &&
            latitude.isFinite() &&
            latitude in -90.0..90.0

    val longitudeValid = longitude != null &&
            longitude.isFinite() &&
            longitude in -180.0..180.0

    val clientError = submitted && clientName.isBlank()
    val addressError = submitted && address.isBlank()
    val descriptionError = submitted && jobDescription.isBlank()
    val latitudeError = submitted && !latitudeValid
    val longitudeError = submitted && !longitudeValid

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "NEW SERVICE REQUEST",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Plan the next fix.",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Add a client, describe the job, and set its location.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Scheduled service date",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = scheduledDate,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Client & service",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    TaskFormField(
                        value = clientName,
                        onValueChange = {
                            clientName = it
                            onDismissError()
                        },
                        label = "Client name",
                        enabled = !isSaving,
                        error = if (clientError) {
                            "Enter the client's name."
                        } else null
                    )

                    TaskFormField(
                        value = address,
                        onValueChange = {
                            address = it
                            onDismissError()
                        },
                        label = "Service address",
                        enabled = !isSaving,
                        singleLine = false,
                        error = if (addressError) {
                            "Enter the service address."
                        } else null
                    )

                    TaskFormField(
                        value = jobDescription,
                        onValueChange = {
                            jobDescription = it
                            onDismissError()
                        },
                        label = "Job description",
                        enabled = !isSaving,
                        singleLine = false,
                        minLines = 3,
                        error = if (descriptionError) {
                            "Describe the work required."
                        } else null
                    )

                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Low", "Normal", "High").forEach { option ->
                            FilterChip(
                                selected = priority == option,
                                onClick = { priority = option },
                                enabled = !isSaving,
                                label = { Text(option) }
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Client location",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Enter coordinates for the service address. " +
                                "Ottawa coordinates are approximately 45.42, -75.69.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    TaskFormField(
                        value = latitudeText,
                        onValueChange = {
                            latitudeText = it
                            onDismissError()
                        },
                        label = "Latitude",
                        enabled = !isSaving,
                        error = if (latitudeError) {
                            "Enter a number from -90 to 90."
                        } else null
                    )

                    TaskFormField(
                        value = longitudeText,
                        onValueChange = {
                            longitudeText = it
                            onDismissError()
                        },
                        label = "Longitude",
                        enabled = !isSaving,
                        error = if (longitudeError) {
                            "Enter a number from -180 to 180."
                        } else null
                    )

                    Text(
                        text = "Use a decimal point and a minus sign for west longitude.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        TextButton(onClick = onDismissError) {
                            Text("Dismiss")
                        }
                    }
                }
            }

            Button(
                onClick = {
                    submitted = true

                    val formValid =
                        clientName.isNotBlank() &&
                                address.isNotBlank() &&
                                jobDescription.isNotBlank() &&
                                latitudeValid &&
                                longitudeValid

                    if (formValid && latitude != null && longitude != null) {
                        onSaveTask(
                            TaskEntity(
                                clientName = clientName.trim(),
                                address = address.trim(),
                                jobDescription = jobDescription.trim(),
                                latitude = latitude,
                                longitude = longitude,
                                scheduledDate = scheduledDate,
                                priority = priority
                            )
                        )
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Text(
                    text = if (isSaving) "Saving task..." else "Save service task",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                text = "Tasks are stored on this device and remain available offline.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Keeps form fields consistent and shows errors beside the relevant input.
 *
 * Text keyboard supports signed coordinates across keyboard apps.
 */
@Composable
private fun TaskFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    error: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error) }
        } else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text
        ),
        shape = MaterialTheme.shapes.small
    )
}