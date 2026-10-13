package me.itzvirtual.myunitox.data.calendar.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "calendar_config")
data class CalendarConfig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val calendarLinkId: String,
)
