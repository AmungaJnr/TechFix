package com.cst3115.enterprise.techfix.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cst3115.enterprise.techfix.Data.data.TaskRepository
import com.cst3115.enterprise.techfix.Data.local.TaskEntity
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Holds task data and handles user actions independently of the UI.
 */
class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    val selectedDate: String = LocalDate.now().toString()

    // Observe the selected day's tasks from Room.
    val tasks = repository.observeTasksForDate(selectedDate)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // Task saving state.
    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    // Initial loading state.
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    // Errors related to loading or saving tasks.
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Separate state for the simulated network update.
    private val _dispatchState = MutableStateFlow(DispatchUiState())
    val dispatchState = _dispatchState.asStateFlow()

    init {
        loadDemoTasks()
    }

    /**
     * Adds demonstration tasks if the selected day is empty.
     */
    private fun loadDemoTasks() {
        viewModelScope.launch {
            try {
                repository.seedDemoTasks(selectedDate)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _errorMessage.value =
                    exception.message ?: "Unable to load demonstration tasks."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Observes one task for a details screen.
     */
    fun observeTask(taskId: Long) =
        repository.observeTaskById(taskId)

    /**
     * Saves a new task and invokes the callback after a successful insert.
     */
    fun addTask(
        task: TaskEntity,
        onSuccess: () -> Unit
    ) {
        if (_isSaving.value) return

        _isSaving.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                repository.addTask(task)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _errorMessage.value =
                    exception.message ?: "Unable to save the task."

                return@launch
            } finally {
                _isSaving.value = false
            }

            onSuccess()
        }
    }

    /**
     * Saves resolution notes and marks a task as completed.
     */
    fun completeTask(
        taskId: Long,
        notes: String,
        onSuccess: () -> Unit
    ) {
        if (_isSaving.value) return

        _isSaving.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                repository.completeTask(taskId, notes)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _errorMessage.value =
                    exception.message ?: "Unable to complete the task."

                return@launch
            } finally {
                _isSaving.value = false
            }

            onSuccess()
        }
    }

    /**
     * Clears a task-related error after dismissal.
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Sends today's task totals to the test API.
     * Updates the dispatch card with loading, success, or error state.
     */
    fun sendDemoDispatchUpdate() {
        if (
            _dispatchState.value.isSending ||
            _isSaving.value ||
            _isLoading.value
        ) {
            return
        }

        _dispatchState.value = DispatchUiState(
            isSending = true
        )

        viewModelScope.launch {
            try {
                val responseId = repository.sendDemoDispatchUpdate(
                    selectedDate
                )

                val sentAt = LocalTime.now().format(
                    DateTimeFormatter.ofPattern("HH:mm:ss")
                )

                _dispatchState.value = DispatchUiState(
                    message = "Snapshot sent at $sentAt. " +
                            "Demo acknowledgement #$responseId. " +
                            "The test server does not store this update."
                )
            } catch (exception: CancellationException) {
                _dispatchState.value = DispatchUiState()
                throw exception
            } catch (exception: IOException) {
                _dispatchState.value = DispatchUiState(
                    message = "Could not connect. " +
                            "Check your internet connection and retry.",
                    isError = true
                )
            } catch (exception: HttpException) {
                _dispatchState.value = DispatchUiState(
                    message = "The test server returned " +
                            "HTTP ${exception.code()}. Retry shortly.",
                    isError = true
                )
            } catch (exception: Exception) {
                _dispatchState.value = DispatchUiState(
                    message = "The demo update could not be processed. " +
                            "Please retry.",
                    isError = true
                )
            }
        }
    }
}

/**
 * A separate data class in the same file.
 * Describes what the dispatch card should display.
 */
data class DispatchUiState(
    val isSending: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)