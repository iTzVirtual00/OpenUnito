package me.itzvirtual.myunitox.data.config.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_config WHERE id = 0")
    fun getConfig(): Flow<AppConfig?>

    @Upsert
    suspend fun saveConfig(config: AppConfig)

    @Query("UPDATE app_config SET authToken = :token WHERE id = 0")
    suspend fun updateToken(token: String?)

    @Query("DELETE FROM app_config")
    suspend fun clearAll()

    // Synchronous query for OkHttp interceptor (runs on OkHttp IO thread, not main)
    @Query("SELECT authToken FROM app_config WHERE id = 0")
    fun getTokenBlocking(): String?

    @Query("SELECT authToken FROM app_config WHERE id = 0")
    suspend fun getToken(): String?

    @Query("UPDATE app_config SET selectedCalendarId = :calendarId WHERE id = 0")
    suspend fun updateSelectedCalendarId(calendarId: Int?)

    @Query("SELECT selectedCalendarId FROM app_config WHERE id = 0")
    suspend fun getSelectedCalendarId(): Int?
}
