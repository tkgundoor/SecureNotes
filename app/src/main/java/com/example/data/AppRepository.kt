package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val noteDao: NoteDao,
    private val passwordDao: PasswordDao,
    private val intruderLogDao: IntruderLogDao
) {
    // --- Notes ---
    fun getActiveNotes(): Flow<List<NoteEntity>> = noteDao.getActiveNotes()
    fun getVaultNotes(): Flow<List<NoteEntity>> = noteDao.getVaultNotes()
    fun getArchivedNotes(): Flow<List<NoteEntity>> = noteDao.getArchivedNotes()
    suspend fun getNoteById(id: Int): NoteEntity? = noteDao.getNoteById(id)
    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)
    fun getCategories(): Flow<List<String>> = noteDao.getCategories()

    // --- Passwords ---
    fun getAllPasswords(): Flow<List<PasswordEntity>> = passwordDao.getAllPasswords()
    suspend fun getPasswordById(id: Int): PasswordEntity? = passwordDao.getPasswordById(id)
    suspend fun insertPassword(password: PasswordEntity): Long = passwordDao.insertPassword(password)
    suspend fun updatePassword(password: PasswordEntity) = passwordDao.updatePassword(password)
    suspend fun deletePassword(password: PasswordEntity) = passwordDao.deletePassword(password)

    // --- Intruder Logs ---
    fun getAllLogs(): Flow<List<IntruderLogEntity>> = intruderLogDao.getAllLogs()
    suspend fun insertLog(log: IntruderLogEntity): Long = intruderLogDao.insertLog(log)
    suspend fun clearAllLogs() = intruderLogDao.clearAllLogs()
}
