package me.itzvirtual.openunito.data.examdate.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** Singleton cache row for the upcoming exam dates list. */
@Entity(tableName = "exam_date_cache")
data class ExamDateEntity(
    @PrimaryKey val id: Int,
    val data: String,       // JSON-serialized List<ExamDateDto>
    val cachedAt: Long,
)
