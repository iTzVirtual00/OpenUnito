package me.itzvirtual.openunito.data.student.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM student_cache WHERE id = 0")
    fun observe(): Flow<StudentEntity?>

    @Upsert
    suspend fun upsert(entity: StudentEntity)

    @Query("DELETE FROM student_cache")
    suspend fun clearAll()
}
