package me.itzvirtual.myunitox.data.dashboard.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {
    @Query("SELECT * FROM dashboard_cache WHERE id = 0")
    fun get(): Flow<DashboardEntity?>

    @Query("SELECT * FROM dashboard_cache WHERE id = 0")
    suspend fun getOnce(): DashboardEntity?

    @Upsert
    suspend fun upsert(entity: DashboardEntity)

    @Query("DELETE FROM dashboard_cache")
    suspend fun clearAll()
}
