package com.cst3115.enterprise.techfix.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cst3115.enterprise.techfix.Data.data.TaskRepository
import com.cst3115.enterprise.techfix.Data.local.TaskEntity
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Holds task data and handles user actions independently of the UI.
 */
class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    // The demonstration dashboard shows the day this ViewModel was created.
    val selectedDate: String = LocalDate.now().toString()

    val tasks = repository.observeTasksForDate(selectedDate)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        loadDemoTasks()
    }

    /**
     * Seeds demonstration data without blocking the UI.
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
     * Exposes one task for the details screen.
     */
    fun observeTask(taskId: Long) = repository.observeTaskById(taskId)

    /**
     * Saves a task and calls onSuccess only after the database insert succeeds.
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
     * Saves completion information before reporting success to the screen.
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
     * Clears an error after it has been dismissed or displayed.
     */
    fun clearError() {
        _errorMessage.value = null
    }
}