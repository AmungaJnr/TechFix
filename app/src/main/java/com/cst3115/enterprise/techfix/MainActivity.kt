package com.cst3115.enterprise.techfix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cst3115.enterprise.techfix.Data.data.TaskRepository
import com.cst3115.enterprise.techfix.Data.local.TechFixDatabase
import com.cst3115.enterprise.techfix.ui.screens.AddTaskScreen
import com.cst3115.enterprise.techfix.ui.screens.TodayScreen
import com.cst3115.enterprise.techfix.ui.theme.TechFixTheme
import com.cst3115.enterprise.techfix.ui.viewmodel.TaskViewModel
import com.cst3115.enterprise.techfix.ui.viewmodel.TaskViewModelFactory
import androidx.compose.material.icons.filled.LocationOn
import com.cst3115.enterprise.techfix.ui.screens.ClientMapScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val database = TechFixDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(database.taskDao())
        val factory = TaskViewModelFactory(repository)

        setContent {
            TechFixTheme {
                val taskViewModel: TaskViewModel = viewModel(
                    factory = factory
                )

                TechFixApp(taskViewModel)
            }
        }
    }
}

/**
 * Connects shared ViewModel state to the app's navigation destinations.
 */
@Composable
private fun TechFixApp(
    taskViewModel: TaskViewModel
) {
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: "today"

    val tasks by taskViewModel.tasks.collectAsStateWithLifecycle()
    val isLoading by taskViewModel.isLoading.collectAsStateWithLifecycle()
    val isSaving by taskViewModel.isSaving.collectAsStateWithLifecycle()
    val errorMessage by taskViewModel.errorMessage.collectAsStateWithLifecycle()

    Scaffold(
        // Each screen handles its own top system inset.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = currentRoute == "today",
                    enabled = !isSaving,
                    onClick = {
                        taskViewModel.clearError()

                        if (currentRoute != "today") {
                            navController.popBackStack(
                                route = "today",
                                inclusive = false
                            )
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null
                        )
                    },
                    label = { Text("Today") }
                )

                NavigationBarItem(
                    selected = currentRoute == "map",
                    enabled = !isSaving,
                    onClick = {
                        taskViewModel.clearError()

                        navController.navigate("map") {
                            popUpTo("today") {
                                inclusive = false
                            }

                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text("Map")
                    }
                )

                NavigationBarItem(
                    selected = currentRoute == "add",
                    enabled = !isSaving,
                    onClick = {
                        taskViewModel.clearError()

                        navController.navigate("add") {
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    },
                    label = { Text("Add task") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "today",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("today") {
                TodayScreen(
                    tasks = tasks,
                    selectedDate = taskViewModel.selectedDate,
                    isLoading = isLoading,
                    isSaving = isSaving,
                    errorMessage = errorMessage,
                    onDismissError = taskViewModel::clearError,
                    onCompleteTask = { taskId, notes, onSuccess ->
                        taskViewModel.completeTask(
                            taskId = taskId,
                            notes = notes,
                            onSuccess = onSuccess
                        )
                    }
                )
            }

            composable("map") {
                ClientMapScreen(
                    tasks = tasks
                )
            }

            composable("add") {
                AddTaskScreen(
                    scheduledDate = taskViewModel.selectedDate,
                    isSaving = isSaving,
                    errorMessage = errorMessage,
                    onDismissError = taskViewModel::clearError,
                    onSaveTask = { task ->
                        taskViewModel.addTask(
                            task = task,
                            onSuccess = {
                                navController.popBackStack(
                                    route = "today",
                                    inclusive = false
                                )
                            }
                        )
                    }
                )
            }
        }
    }
}