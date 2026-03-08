package me.itzvirtual.myunitox.data.agenda.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AgendaDao {
    @Query("SELECT * FROM agenda_cache WHERE dateRangeKey = :key")
    suspend fun getByKey(key: String): AgendaEntity?

    @Query("SELECT * FROM agenda_cache WHERE dateRangeKey = :key")
    fun getByKeyFlow(key: String): Flow<AgendaEntity?>

    @Upsert
    suspend fun upsert(entity: AgendaEntity)

    @Query("DELETE FROM agenda_cache")
    suspend fun clearAll()
}
