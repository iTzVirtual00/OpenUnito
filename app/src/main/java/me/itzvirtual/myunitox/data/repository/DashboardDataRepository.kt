package me.itzvirtual.myunitox.data.repository

import me.itzvirtual.myunitox.data.models.DashboardDataResponse
import me.itzvirtual.myunitox.data.models.ExamResult
import me.itzvirtual.myunitox.data.models.GradeStatus
import me.itzvirtual.myunitox.data.remote.UnitoAPI
import javax.inject.Inject

class DashboardDataRepository @Inject constructor(private val api: UnitoAPI) {
    suspend fun fetchDashboardData(): Result<DashboardDataResponse> {
        return try {
            val response = api.getDashboardData()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changeGradeStatus(gradeId: String, gradeStatus: GradeStatus): Result<ExamResult> {
        return try {
            val result = api.changeGradeStatus(gradeId, gradeStatus.toString())
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}