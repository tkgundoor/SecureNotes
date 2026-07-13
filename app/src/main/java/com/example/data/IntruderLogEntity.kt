package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "intruder_logs")
data class IntruderLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val failedPinAttempted: String,
    val photoPath: String? = null // Simulated file path for captured photo
)
