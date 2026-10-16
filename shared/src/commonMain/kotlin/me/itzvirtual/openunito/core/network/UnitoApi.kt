package me.itzvirtual.openunito.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import me.itzvirtual.openunito.data.agenda.remote.AgendaEntryDto
import me.itzvirtual.openunito.data.agenda.remote.PublicAgendaRequest
import me.itzvirtual.openunito.data.dashboard.remote.DashboardDataResponseDto
import me.itzvirtual.openunito.data.examdate.remote.ExamDateDto
import me.itzvirtual.openunito.data.examresult.remote.ExamResultDto
import me.itzvirtual.openunito.data.grade.remote.ExamGradeDto
import me.itzvirtual.openunito.data.student.StudentRepository
import me.itzvirtual.openunito.domain.student.StudentCareer

class UnitoApi(
    private val client: HttpClient,
    private val studentRepository: StudentRepository,
) {

    private suspend fun getCareer(): StudentCareer = studentRepository.getCurrentCareer()

    suspend fun getDashboardData(): DashboardDataResponseDto {
        val career = getCareer()
        return client.get("students/${career.id}/careers/${career.registrationId}/dashboard").body()
    }

    suspend fun getExamBoard(): List<ExamResultDto> {
        val career = getCareer()
        return client.get("students/${career.registrationId}/exams/board").body()
    }

    suspend fun getExamDates(): List<ExamDateDto> {
        val career = getCareer()
        return client.get("students/${career.id}/careers/${career.registrationId}/exams").body()
    }

    suspend fun getTranscript(): List<ExamGradeDto> {
        val career = getCareer()
        return client.get("students/${career.registrationId}/transcript").body()
    }

    suspend fun changeGradeStatus(
        gradeId: String,
        gradeStatus: String,
    ): ExamResultDto {
        val career = getCareer()
        return client.put("students/${career.registrationId}/exams/board/$gradeId/acknowledgement") {
            parameter("presaVisione", gradeStatus)
        }.body()
    }

    suspend fun getPublicAgenda(body: PublicAgendaRequest): List<AgendaEntryDto> {
        return client.post("https://unito.prod.up.cineca.it/api/Impegni/getImpegniCalendarioPubblico") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }
}
