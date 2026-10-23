package me.itzvirtual.openunito.data.config.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_config WHERE id = 0")
    fun getConfig(): Flow<AppConfig?>

    @Upsert
    suspend fun saveConfig(config: AppConfig)

    @Query("UPDATE app_config SET authToken = :token WHERE id = 0")
    suspend fun updateToken(token: String?)

    @Query("UPDATE app_config SET selectedCareerId = :careerId WHERE id = 0")
    suspend fun updateCareerId(careerId: Int?)

    @Query("SELECT selectedCalendarId FROM app_config WHERE id = 0")
    suspend fun getSelectedCalendarId(): Int?

    @Query("UPDATE app_config SET selectedCalendarId = :calendarId WHERE id = 0")
    suspend fun updateSelectedCalendarId(calendarId: Int?)


    @Query("DELETE FROM app_config")
    suspend fun clearAll()
}
