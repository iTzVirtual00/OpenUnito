package me.itzvirtual.myunitox.data.student.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Singleton cache row for the authenticated student session data. */
@Entity(tableName = "student_cache")
data class StudentEntity(
    @PrimaryKey val id: Int = 0,
    val data: String,       // JSON-serialized AuthSessionDto
    val cachedAt: Long,
)
