package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface IntruderLogDao {
    @Query("SELECT * FROM intruder_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<IntruderLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: IntruderLogEntity): Long

    @Query("DELETE FROM intruder_logs")
    suspend fun clearAllLogs()
}
