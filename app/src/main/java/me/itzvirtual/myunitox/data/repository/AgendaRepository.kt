package me.itzvirtual.myunitox.data.repository

import me.itzvirtual.myunitox.data.models.AgendaEntry
import me.itzvirtual.myunitox.data.models.ExamDate
import me.itzvirtual.myunitox.data.models.PublicAgendaRequest
import me.itzvirtual.myunitox.data.remote.UnitoAPI
import java.time.Instant
import javax.inject.Inject

class AgendaRepository @Inject constructor(private val api: UnitoAPI) {
    suspend fun fetchPublicAgenda(
        calendarLinkId: String,
        clientId: String,
        startDate: Instant,
        endDate: Instant
    ): Result<List<AgendaEntry>> {

        return try {
            val response = api.getPublicAgenda(
                PublicAgendaRequest(
                    calendarLinkId,
                    clientId,
                    startDate.toString(),
                    endDate.toString()
                )
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}