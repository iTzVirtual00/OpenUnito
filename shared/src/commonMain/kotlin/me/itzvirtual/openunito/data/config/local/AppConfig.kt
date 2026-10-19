package me.itzvirtual.openunito.data.config.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

// Remember to reflect non-default parameters into DatabaseModule.dbInitialize
@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey val id: Int = 0,
    val authToken: String? = null,
    val selectedCalendarId: Int? = null,
    @ColumnInfo(defaultValue = "0") val debug: Boolean = false,
    // TODO: Add "selectedProfile", default value 0,
    // They give us careers in a list so why not use the index to select it
    // worst case they change smthing and user selects again.
    // It sucks but should never happen
)
