package me.itzvirtual.myunitox.data.examresult.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamResultDao {
    @Query("SELECT * FROM exam_result_cache WHERE id = 0")
    fun get(): Flow<ExamResultEntity?>

    @Query("SELECT * FROM exam_result_cache WHERE id = 0")
    suspend fun getOnce(): ExamResultEntity?

    @Upsert
    suspend fun upsert(entity: ExamResultEntity)

    @Query("DELETE FROM exam_result_cache")
    suspend fun clearAll()
}
