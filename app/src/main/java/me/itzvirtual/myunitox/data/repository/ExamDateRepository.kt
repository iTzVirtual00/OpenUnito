package me.itzvirtual.myunitox.data.repository

import me.itzvirtual.myunitox.data.models.ExamDate
import me.itzvirtual.myunitox.data.remote.UnitoAPI
import javax.inject.Inject

class ExamDateRepository @Inject constructor(private val api: UnitoAPI) {
    suspend fun fetchExamDates(): Result<List<ExamDate>> {
        return try {
            val response = api.getExamDates()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}