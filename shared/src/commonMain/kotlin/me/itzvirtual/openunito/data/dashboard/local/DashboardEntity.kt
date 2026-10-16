package me.itzvirtual.openunito.data.dashboard.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** Singleton cache row for the dashboard data response. */
@Entity(tableName = "dashboard_cache")
data class DashboardEntity(
    @PrimaryKey val id: Int = 0,
    val data: String,       // JSON-serialized DashboardDataResponseDto
    val cachedAt: Long,
)
