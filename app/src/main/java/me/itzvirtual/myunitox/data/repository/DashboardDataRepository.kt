package me.itzvirtual.myunitox.data.repository

import me.itzvirtual.myunitox.data.models.DashboardDataResponse
import me.itzvirtual.myunitox.data.models.ExamDate
import me.itzvirtual.myunitox.data.models.ExamResult
import me.itzvirtual.myunitox.data.models.GradeStatus
import me.itzvirtual.myunitox.data.remote.UnitoAPI
import me.itzvirtual.myunitox.ui.util.MemoizedFetcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardDataRepository @Inject constructor(private val api: UnitoAPI) {

    private val cache = MemoizedFetcher<Unit, Result<DashboardDataResponse>> {
        internalFetchDashboardData()
    }

    suspend fun fetchDashboardData(useCache: Boolean = true): Result<DashboardDataResponse> {
        return cache.execute(Unit, !useCache)
    }

    private suspend fun internalFetchDashboardData(): Result<DashboardDataResponse> {
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