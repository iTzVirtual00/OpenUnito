package me.itzvirtual.myunitox.data.grade.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Singleton cache row for the exam grades (transcript) list. */
@Entity(tableName = "exam_grade_cache")
data class ExamGradeEntity(
    @PrimaryKey val id: Int = 0,
    val data: String,       // JSON-serialized List<ExamGradeDto>
    val cachedAt: Long,
)
