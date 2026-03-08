package me.itzvirtual.myunitox.data.config.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey val id: Int = 0,
    val authToken: String? = null,
    val calendarLinkId: String? = null,
    val calendarClientId: String = "5852fd1ab7305612a8354d51",
)
