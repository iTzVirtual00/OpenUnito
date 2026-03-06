package me.itzvirtual.myunitox.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey val id: Int = 0, // Singleton row pattern
    val authToken: String? = null,
    val calendarLinkId: String? = null,
    val calendarClientId: String = "5852fd1ab7305612a8354d51"
)
