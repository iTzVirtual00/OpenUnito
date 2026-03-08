package me.itzvirtual.myunitox.data.dashboard.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Singleton cache row for the dashboard data response. */
@Entity(tableName = "dashboard_cache")
data class DashboardEntity(
    @PrimaryKey val id: Int = 0,
    val data: String,       // JSON-serialized DashboardDataResponseDto
    val cachedAt: Long,
)
