package com.cst3115.enterprise.techfix.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cst3115.enterprise.techfix.Data.local.TaskEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.cst3115.enterprise.techfix.ui.viewmodel.DispatchUiState

/**
 * Displays daily workload, search, filters, and task details.
 * Database operations are supplied by the parent through callbacks.
 */
@Composable
fun TodayScreen(
    tasks: List<TaskEntity>,
    selectedDate: String,
    isLoading: Boolean,
    isSaving: Boolean,
    errorMessage: String?,
    dispatchState: DispatchUiState,
    onSendDispatchUpdate: () -> Unit,
    onDismissError: () -> Unit,
    onCompleteTask: (Long, String, () -> Unit) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf("All") }
    var selectedTaskId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    val completedCount = tasks.count { it.isCompleted }
    val pendingCount = tasks.size - completedCount

    val filteredTasks = tasks.filter { task ->
        val matchesSearch =
            task.clientName.contains(query.trim(), ignoreCase = true) ||
                    task.address.contains(query.trim(), ignoreCase = true) ||
                    task.jobDescription.contains(query.trim(), ignoreCase = true)

        val matchesStatus = when (selectedFilter) {
            "Pending" -> !task.isCompleted
            "Completed" -> task.isCompleted
            else -> true
        }

        matchesSearch && matchesStatus
    }

    val selectedTask = tasks.firstOrNull {
        it.id == selectedTaskId
    }

    val formattedDate = LocalDate.parse(selectedDate).format(
        DateTimeFormatter.ofPattern(
            "EEEE, MMMM d",
            Locale.getDefault()
        )
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "TECHFIX / FIELD SERVICE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Your day, organized.",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                WorkloadCard(
                    total = tasks.size,
                    completed = completedCount,
                    pending = pendingCount
                )
            }

            item {
                DispatchStatusCard(
                    state = dispatchState,
                    enabled = !isLoading && !isSaving,
                    onSend = onSendDispatchUpdate
                )
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search your tasks") },
                    placeholder = {
                        Text("Client, address, or job")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            TextButton(
                                onClick = { query = "" }
                            ) {
                                Text("Clear")
                            }
                        }
                    }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Pending", "Completed").forEach { filter ->
                        val count = when (filter) {
                            "Pending" -> pendingCount
                            "Completed" -> completedCount
                            else -> tasks.size
                        }

                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text("$filter ($count)") }
                        )
                    }
                }
            }

            // When a dialog is open, it displays the error near its form.
            if (errorMessage != null && selectedTask == null) {
                item {
                    ErrorCard(
                        message = errorMessage,
                        onDismiss = onDismissError
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Service tasks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${filteredTasks.size} shown",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (filteredTasks.isEmpty()) {
                item {
                    EmptyTasksCard(
                        hasTasks = tasks.isNotEmpty()
                    )
                }
            } else {
                items(
                    items = filteredTasks,
                    key = { task -> task.id }
                ) { task ->
                    TaskCard(
                        task = task,
                        onClick = {
                            onDismissError()
                            selectedTaskId = task.id
                        }
                    )
                }
            }
        }
    }

    if (selectedTask != null) {
        TaskDetailsDialog(
            task = selectedTask,
            isSaving = isSaving,
            errorMessage = errorMessage,
            onDismissError = onDismissError,
            onDismiss = {
                selectedTaskId = null
                onDismissError()
            },
            onComplete = { notes ->
                onCompleteTask(selectedTask.id, notes) {
                    selectedTaskId = null
                }
            }
        )
    }
}

/**
 * Summarizes the whole day's workload, independent of list filters.
 */
@Composable
private fun WorkloadCard(
    total: Int,
    completed: Int,
    pending: Int
) {
    val progress = if (total == 0) {
        0f
    } else {
        completed.toFloat() / total
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "TODAY'S OVERVIEW",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = when {
                    total == 0 -> "Ready for your first task"
                    pending == 0 -> "All tasks completed"
                    pending == 1 -> "1 task to go"
                    else -> "$pending tasks to go"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )

            Text(
                text = "$completed of $total completed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.primary.copy(
                    alpha = 0.15f
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WorkloadMetric("Assigned", total)
                WorkloadMetric("Pending", pending)
                WorkloadMetric("Completed", completed)
            }
        }
    }
}

@Composable
private fun WorkloadMetric(
    label: String,
    value: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * Displays a concise task summary and opens its details when tapped.
 */
@Composable
private fun TaskCard(
    task: TaskEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TaskBadge(
                    text = if (task.isCompleted) "Completed" else "Pending",
                    success = task.isCompleted
                )

                TaskBadge(
                    text = "${task.priority} priority",
                    urgent = task.priority == "High"
                )
            }

            Text(
                text = task.clientName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = task.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = task.jobDescription,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Text(
                text = if (task.isCompleted) {
                    "View service summary"
                } else {
                    "View task and complete"
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Uses both text and colour to communicate status or priority.
 */
@Composable
private fun TaskBadge(
    text: String,
    success: Boolean = false,
    urgent: Boolean = false
) {
    val background = when {
        success -> MaterialTheme.colorScheme.secondaryContainer
        urgent -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val foreground = when {
        success -> MaterialTheme.colorScheme.onSecondaryContainer
        urgent -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Text(
        text = text,
        modifier = Modifier
            .background(
                color = background,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelMedium,
        color = foreground
    )
}

/**
 * Shows the full job and collects resolution notes before completion.
 */
@Composable
private fun TaskDetailsDialog(
    task: TaskEntity,
    isSaving: Boolean,
    errorMessage: String?,
    onDismissError: () -> Unit,
    onDismiss: () -> Unit,
    onComplete: (String) -> Unit
) {
    var notes by rememberSaveable(task.id) {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
        title = {
            Text(
                text = task.clientName,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TaskBadge(
                    text = if (task.isCompleted) "Completed" else "Pending",
                    success = task.isCompleted
                )

                Text(
                    text = task.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Job description",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(text = task.jobDescription)

                if (task.isCompleted) {
                    Text(
                        text = "Resolution notes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(text = task.completionNotes)
                } else {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = {
                            notes = it
                            if (errorMessage != null) onDismissError()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving,
                        label = { Text("Resolution notes") },
                        placeholder = {
                            Text("What did you fix or verify?")
                        },
                        supportingText = {
                            Text("Required to complete this task.")
                        },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            if (!task.isCompleted) {
                Button(
                    onClick = { onComplete(notes) },
                    enabled = !isSaving && notes.isNotBlank()
                ) {
                    Text(
                        text = if (isSaving) {
                            "Saving..."
                        } else {
                            "Complete task"
                        }
                    )
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            if (!task.isCompleted) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isSaving
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
private fun EmptyTasksCard(
    hasTasks: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (hasTasks) "No matching tasks" else "No tasks yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (hasTasks) {
                    "Try another search or status filter."
                } else {
                    "Your scheduled service tasks will appear here."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}