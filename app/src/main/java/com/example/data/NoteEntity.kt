package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String, // Encrypted if isVault = true or isLocked = true
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isVault: Boolean = false,
    val isLocked: Boolean = false,
    val category: String = "General",
    val reminderTime: Long? = null,
    val lastModified: Long = System.currentTimeMillis(),
    val checklistJson: String? = null, // JSON representing items in a checklist
    val attachmentsJson: String? = null // JSON representing secure attachments (images, files)
)
