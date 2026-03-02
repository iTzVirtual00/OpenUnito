package me.itzvirtual.myunitox.data.repository

import me.itzvirtual.myunitox.data.models.ExamGrade
import me.itzvirtual.myunitox.data.remote.UnitoAPI
import me.itzvirtual.myunitox.ui.util.MemoizedFetcher
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class GradeRepository @Inject constructor(private val api: UnitoAPI) {
    private val cache = MemoizedFetcher<Unit, Result<List<ExamGrade>>> {
        internalFetchGrades()
    }

    suspend fun fetchGrades(useCache: Boolean = true): Result<List<ExamGrade>> {
        return cache.execute(Unit, !useCache)
    }
    private suspend fun internalFetchGrades(): Result<List<ExamGrade>> {
        return try {
            val response = api.getTranscript()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}