package me.itzvirtual.myunitox.data.grade.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamGradeDao {
    @Query("SELECT * FROM exam_grade_cache WHERE id = 0")
    fun get(): Flow<ExamGradeEntity?>

    @Query("SELECT * FROM exam_grade_cache WHERE id = 0")
    suspend fun getOnce(): ExamGradeEntity?

    @Upsert
    suspend fun upsert(entity: ExamGradeEntity)

    @Query("DELETE FROM exam_grade_cache")
    suspend fun clearAll()
}
