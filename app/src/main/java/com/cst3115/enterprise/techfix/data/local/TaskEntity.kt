package com.cst3115.enterprise.techfix.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Represents one service task stored in the local Room database.
 *
 * Coordinates support client map markers and directions.
 * Completion fields preserve the technician's resolution notes.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val clientName: String,
    val address: String,
    val jobDescription: String,

    val latitude: Double,
    val longitude: Double,

    // ISO date format: yyyy-MM-dd.
    val scheduledDate: String = LocalDate.now().toString(),

    // The UI will offer controlled priority choices.
    val priority: String = "Normal",

    val isCompleted: Boolean = false,
    val completionNotes: String = "",
    val completedAt: Long? = null
)