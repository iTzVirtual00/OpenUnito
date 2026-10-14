package me.itzvirtual.myunitox.data.agenda.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
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

    @Query("DELETE FROM agenda_cache WHERE cachedAt < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}
