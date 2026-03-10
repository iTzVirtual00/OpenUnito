package me.itzvirtual.myunitox.data.calendar.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_config")
data class CalendarConfig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val calendarLinkId: String,
)
