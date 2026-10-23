package me.itzvirtual.openunito.data.grade.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamGradeDao {
    @Query("SELECT * FROM exam_grade_cache WHERE id = :careerId")
    fun observe(careerId: Int): Flow<ExamGradeEntity?>

    @Upsert
    suspend fun upsert(entity: ExamGradeEntity)

    @Query("DELETE FROM exam_grade_cache")
    suspend fun clearAll()
}
