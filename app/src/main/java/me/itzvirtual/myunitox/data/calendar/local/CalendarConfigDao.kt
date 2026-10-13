package me.itzvirtual.myunitox.data.calendar.local

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarConfigDao {
    @Query("SELECT * FROM calendar_config ORDER BY name ASC")
    fun getAllFlow(): Flow<List<CalendarConfig>>

    @Query("SELECT * FROM calendar_config ORDER BY name ASC")
    suspend fun getAll(): List<CalendarConfig>

    @Query("SELECT * FROM calendar_config WHERE id = :id")
    suspend fun getById(id: Int): CalendarConfig?

    @Upsert
    suspend fun upsert(config: CalendarConfig): Long

    @Delete
    suspend fun delete(config: CalendarConfig)

    @Query("DELETE FROM calendar_config WHERE id = :id")
    suspend fun deleteById(id: Int)
}
