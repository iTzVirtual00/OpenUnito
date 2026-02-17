package me.itzvirtual.myunitox.data.repository

import me.itzvirtual.myunitox.data.models.ExamGrade
import me.itzvirtual.myunitox.data.remote.UnitoAPI
import javax.inject.Inject


class GradeRepository @Inject constructor(private val api: UnitoAPI) {
    suspend fun fetchGrades(): Result<List<ExamGrade>> {
        return try {
            val response = api.getTranscript()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}