package me.itzvirtual.openunito.data.config.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

// Remember to reflect non-default parameters into DatabaseModule.dbInitialize
@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey val id: Int,
    val authToken: String? = null,
    val selectedCalendarId: Int? = null,
    val selectedCareerId: Int? = null,
    @ColumnInfo(defaultValue = "0") val debug: Boolean = false,
)