package me.itzvirtual.myunitox.data.repository

import me.itzvirtual.myunitox.data.models.ExamDate
import me.itzvirtual.myunitox.data.remote.UnitoAPI
import me.itzvirtual.myunitox.ui.util.MemoizedFetcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamDateRepository @Inject constructor(private val api: UnitoAPI) {

    private val cache = MemoizedFetcher<Unit, Result<List<ExamDate>>> {
        internalFetchExamDates()
    }

    suspend fun fetchExamDates(useCache: Boolean = true): Result<List<ExamDate>> {
        return cache.execute(Unit, !useCache)
    }

    private suspend fun internalFetchExamDates(): Result<List<ExamDate>> {
        return try {
            val response = api.getExamDates()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}