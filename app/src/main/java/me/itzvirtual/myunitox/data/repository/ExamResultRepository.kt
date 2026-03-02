package me.itzvirtual.myunitox.data.repository

import me.itzvirtual.myunitox.data.models.ExamResult
import me.itzvirtual.myunitox.data.remote.UnitoAPI
import me.itzvirtual.myunitox.ui.util.MemoizedFetcher
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ExamResultRepository @Inject constructor(private val api: UnitoAPI) {
    private val cache = MemoizedFetcher<Unit, Result<List<ExamResult>>> {
        internalFetchExamResults()
    }

    suspend fun fetchExamResults(useCache: Boolean = true):  Result<List<ExamResult>> {
        return cache.execute(Unit, !useCache)
    }
    suspend fun internalFetchExamResults(): Result<List<ExamResult>> {
        return try {
            val response = api.getExamBoard()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}