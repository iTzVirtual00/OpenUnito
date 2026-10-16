package me.itzvirtual.openunito.data.student.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** Singleton cache row for the authenticated student session data. */
@Entity(tableName = "student_cache")
data class StudentEntity(
    @PrimaryKey val id: Int = 0,
    val data: String,       // JSON-serialized AuthSessionDto
    val cachedAt: Long,
)
