package me.itzvirtual.openunito.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.flow.first
import me.itzvirtual.openunito.data.agenda.remote.AgendaEntryDto
import me.itzvirtual.openunito.data.agenda.remote.PublicAgendaRequest
import me.itzvirtual.openunito.data.dashboard.remote.DashboardDataResponseDto
import me.itzvirtual.openunito.data.examdate.remote.BookExamRequest
import me.itzvirtual.openunito.data.examdate.remote.ExamBookingResponse
import me.itzvirtual.openunito.data.examdate.remote.ExamDateDto
import me.itzvirtual.openunito.data.examresult.remote.ExamResultDto
import me.itzvirtual.openunito.data.grade.remote.ExamGradeDto
import me.itzvirtual.openunito.data.student.StudentRepository
import me.itzvirtual.openunito.domain.examdate.ExamDate
import me.itzvirtual.openunito.domain.examdate.ExamDateId

data class CareerContext(
    val id: Int,
    val registrationId: Int
)

// Main api needed for literally anything
class UnitoApi(
    private val client: HttpClient,
    private val studentRepository: StudentRepository,
) {
    // .first() hangs until we have a valid career
    private suspend fun getCareerContext(): CareerContext =
        studentRepository.careerContextFlow.first()

    suspend fun getDashboardData(context: CareerContext? = null): DashboardDataResponseDto {
        val ctx = context ?: getCareerContext()
        return client.get("students/${ctx.id}/careers/${ctx.registrationId}/dashboard").body()
    }

    suspend fun getExamBoard(context: CareerContext? = null): List<ExamResultDto> {
        val ctx = context ?: getCareerContext()
        return client.get("students/${ctx.registrationId}/exams/board").body()
    }

    suspend fun getExamDates(context: CareerContext? = null): List<ExamDateDto> {
        val ctx = context ?: getCareerContext()
        return client.get("students/${ctx.id}/careers/${ctx.registrationId}/exams").body()
    }

    suspend fun getSingleExam(examDate: ExamDateDto): ExamDateDto = getSingleExam(null, examDate.adsceId, examDate.cdsAppId, examDate.adAppId, examDate.appId)
    suspend fun getSingleExam(context: CareerContext? = null, adsceId: Long, cdsAppId: Long, adAppId: Long, appId: Long): ExamDateDto {
        val ctx = context ?: getCareerContext()
        return client.get("students/${ctx.id}/careers/${ctx.registrationId}/exams/${adsceId}/${cdsAppId}/${adAppId}/${appId}").body()
    }

    suspend fun getTranscript(context: CareerContext? = null): List<ExamGradeDto> {
        val ctx = context ?: getCareerContext()
        return client.get("students/${ctx.registrationId}/transcript").body()
    }

    suspend fun changeGradeStatus(
        gradeId: String,
        gradeStatus: String,
        context: CareerContext? = null,
    ): ExamResultDto {
        val ctx = context ?: getCareerContext()
        return client.put("students/${ctx.registrationId}/exams/board/$gradeId/acknowledgement") {
            parameter("presaVisione", gradeStatus)
        }.body()
    }

    suspend fun getPublicAgenda(body: PublicAgendaRequest): List<AgendaEntryDto> {
        return client.post("https://unito.prod.up.cineca.it/api/Impegni/getImpegniCalendarioPubblico") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    suspend fun bookExam(
        exam: ExamDate,
        context: CareerContext? = null,
    ): ExamBookingResponse{
        val ctx = context ?: getCareerContext()
        val requestBody = BookExamRequest()
        return client.put("students/${ctx.id}/exams/${exam.adsceId}/sessions/${exam.cdsAppId},${exam.adAppId},${exam.appId}") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

    suspend fun unbookExam(
        exam: ExamDate,
        context: CareerContext? = null,
    ): ExamBookingResponse{
        val ctx = context ?: getCareerContext()
        return client.delete("students/${ctx.id}/exams/${exam.adsceId}/sessions/${exam.cdsAppId},${exam.adAppId},${exam.appId}").body()
    }

    suspend fun logout(): HttpStatusCode {
        return client.get("auth/logout").status
    }
}
