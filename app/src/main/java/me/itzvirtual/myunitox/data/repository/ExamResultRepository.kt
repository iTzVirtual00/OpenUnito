package me.itzvirtual.myunitox.data.repository

import me.itzvirtual.myunitox.data.models.ExamResult
import me.itzvirtual.myunitox.data.remote.UnitoAPI
import javax.inject.Inject


class ExamResultRepository @Inject constructor(private val api: UnitoAPI) {
    suspend fun fetchExamResults(): Result<List<ExamResult>> {
        return try {
            val response = api.getExamBoard()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}