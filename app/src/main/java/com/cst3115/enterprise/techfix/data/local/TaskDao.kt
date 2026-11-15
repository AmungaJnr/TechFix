package com.cst3115.enterprise.techfix.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Defines the database operations available for service tasks.
 * Room generates the implementation through KSP.
 */
@Dao
interface TaskDao {

    /**
     * Observes tasks for a selected day.
     * Pending tasks appear first, followed by priority and creation order.
     */
    @Query(
        """
        SELECT * FROM tasks
        WHERE scheduledDate = :date
        ORDER BY
            isCompleted ASC,
            CASE priority
                WHEN 'High' THEN 0
                WHEN 'Normal' THEN 1
                ELSE 2
            END ASC,
            id ASC
        """
    )
    fun observeTasksForDate(date: String): Flow<List<TaskEntity>>

    /**
     * Observes one task by its unique ID.
     * Emits null if that task does not exist.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun observeTaskById(taskId: Long): Flow<TaskEntity?>

    /**
     * Inserts a task and returns its generated database ID.
     */
    @Insert
    suspend fun insertTask(task: TaskEntity): Long

    /**
     * Saves completion status, resolution notes, and completion time together.
     * Returns the number of rows updated.
     */
    @Query(
        """
        UPDATE tasks
        SET isCompleted = 1,
            completionNotes = :notes,
            completedAt = :completedAt
        WHERE id = :taskId AND isCompleted = 0
        """
    )
    suspend fun completeTask(
        taskId: Long,
        notes: String,
        completedAt: Long
    ): Int
}