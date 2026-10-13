package me.itzvirtual.myunitox.data.examresult.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** Singleton cache row for the exam results (board) list. */
@Entity(tableName = "exam_result_cache")
data class ExamResultEntity(
    @PrimaryKey val id: Int = 0,
    val data: String,       // JSON-serialized List<ExamResultDto>
    val cachedAt: Long,
)
