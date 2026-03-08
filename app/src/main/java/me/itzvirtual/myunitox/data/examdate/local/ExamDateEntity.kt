package me.itzvirtual.myunitox.data.examdate.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Singleton cache row for the upcoming exam dates list. */
@Entity(tableName = "exam_date_cache")
data class ExamDateEntity(
    @PrimaryKey val id: Int = 0,
    val data: String,       // JSON-serialized List<ExamDateDto>
    val cachedAt: Long,
)
