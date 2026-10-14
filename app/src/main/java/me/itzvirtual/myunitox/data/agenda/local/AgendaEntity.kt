package me.itzvirtual.myunitox.data.agenda.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** Caches a list of agenda entries for a given date range key (e.g. "2026-02-15_2026-02-22"). */
@Entity(tableName = "agenda_cache")
data class AgendaEntity(
    @PrimaryKey val dateRangeKey: String,
    val data: String,       // JSON-serialized List<AgendaEntryDto>
    val cachedAt: Long,
)
