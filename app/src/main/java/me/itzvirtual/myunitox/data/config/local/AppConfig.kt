package me.itzvirtual.myunitox.data.config.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey val id: Int = 0,
    val authToken: String? = null,
    val selectedCalendarId: Int? = null,
    @ColumnInfo(defaultValue = "0") val debug: Boolean = false,
)
