package me.itzvirtual.openunito.data.examdate.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDateDao {
    @Query("SELECT * FROM exam_date_cache WHERE id = 0")
    fun get(): Flow<ExamDateEntity?>

    @Query("SELECT * FROM exam_date_cache WHERE id = 0")
    suspend fun getOnce(): ExamDateEntity?

    @Upsert
    suspend fun upsert(entity: ExamDateEntity)

    @Query("DELETE FROM exam_date_cache")
    suspend fun clearAll()
}
