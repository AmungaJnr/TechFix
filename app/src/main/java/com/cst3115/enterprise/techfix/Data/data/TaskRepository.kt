package com.cst3115.enterprise.techfix.Data.data

import com.cst3115.enterprise.techfix.Data.local.TaskDao
import com.cst3115.enterprise.techfix.Data.local.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Provides a single access point for task data.
 * Screens and ViewModels do not access the DAO directly.
 */
class TaskRepository(
    private val taskDao: TaskDao
) {

    fun observeTasksForDate(date: String): Flow<List<TaskEntity>> {
        return taskDao.observeTasksForDate(date)
    }

    fun observeTaskById(taskId: Long): Flow<TaskEntity?> {
        return taskDao.observeTaskById(taskId)
    }

    /**
     * Validates and cleans task data before saving it.
     */
    suspend fun addTask(task: TaskEntity): Long {
        require(task.clientName.isNotBlank()) {
            "Enter a client name."
        }

        require(task.address.isNotBlank()) {
            "Enter a client address."
        }

        require(task.jobDescription.isNotBlank()) {
            "Enter a job description."
        }

        require(task.latitude.isFinite() && task.latitude in -90.0..90.0) {
            "Enter a valid latitude."
        }

        require(task.longitude.isFinite() && task.longitude in -180.0..180.0) {
            "Enter a valid longitude."
        }

        require(task.priority in listOf("High", "Normal", "Low")) {
            "Choose a valid priority."
        }

        return taskDao.insertTask(
            task.copy(
                clientName = task.clientName.trim(),
                address = task.address.trim(),
                jobDescription = task.jobDescription.trim()
            )
        )
    }

    /**
     * Requires a resolution note before completing a task.
     */
    suspend fun completeTask(taskId: Long, notes: String) {
        val cleanedNotes = notes.trim()

        require(cleanedNotes.isNotEmpty()) {
            "Describe how the issue was resolved."
        }

        val updatedRows = taskDao.completeTask(
            taskId = taskId,
            notes = cleanedNotes,
            completedAt = System.currentTimeMillis()
        )

        check(updatedRows == 1) {
            "This task was already completed or could not be found."
        }
    }

    /**
     * Creates fictional clients at demonstration locations in Ottawa.
     */
    suspend fun seedDemoTasks(date: String) {
        val demoTasks = listOf(
            TaskEntity(
                clientName = "Northstar Office",
                address = "110 Laurier Avenue West, Ottawa",
                jobDescription = "Restore network access and test workstation connectivity.",
                latitude = 45.4208,
                longitude = -75.6904,
                scheduledDate = date,
                priority = "High"
            ),
            TaskEntity(
                clientName = "Maple Studio",
                address = "55 ByWard Market Square, Ottawa",
                jobDescription = "Set up a shared printer and verify printing from two laptops.",
                latitude = 45.4274,
                longitude = -75.6924,
                scheduledDate = date,
                priority = "Normal"
            ),
            TaskEntity(
                clientName = "Cedar Consulting",
                address = "100 Elgin Street, Ottawa",
                jobDescription = "Install software updates and check the workstation backup.",
                latitude = 45.4215,
                longitude = -75.6942,
                scheduledDate = date,
                priority = "Low"
            )
        )

        taskDao.seedTasksIfEmpty(
            date = date,
            tasks = demoTasks
        )
    }
}