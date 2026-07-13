package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class PasswordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String, // Website / App name
    val username: String,
    val encryptedPassword: String,
    val notes: String = "",
    val otpSecret: String? = null,
    val strength: String = "Medium", // Weak, Medium, Strong
    val lastModified: Long = System.currentTimeMillis()
)
