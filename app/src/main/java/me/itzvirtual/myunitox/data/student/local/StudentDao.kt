package me.itzvirtual.myunitox.data.student.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM student_cache WHERE id = 0")
    fun get(): Flow<StudentEntity?>

    @Query("SELECT * FROM student_cache WHERE id = 0")
    fun getOneShot(): StudentEntity?

    @Upsert
    suspend fun upsert(entity: StudentEntity)

    @Query("DELETE FROM student_cache")
    suspend fun clearAll()
}
