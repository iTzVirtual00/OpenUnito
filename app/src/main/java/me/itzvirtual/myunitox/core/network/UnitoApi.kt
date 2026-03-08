package me.itzvirtual.myunitox.core.network

import me.itzvirtual.myunitox.data.agenda.remote.AgendaEntryDto
import me.itzvirtual.myunitox.data.agenda.remote.PublicAgendaRequest
import me.itzvirtual.myunitox.data.dashboard.remote.DashboardDataResponseDto
import me.itzvirtual.myunitox.data.examdate.remote.ExamDateDto
import me.itzvirtual.myunitox.data.examresult.remote.ExamResultDto
import me.itzvirtual.myunitox.data.grade.remote.ExamGradeDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UnitoApi {
    @GET("students/{careerId}/careers/{registrationId}/dashboard")
    suspend fun getDashboardData(): DashboardDataResponseDto

    @GET("students/{registrationId}/exams/board")
    suspend fun getExamBoard(): List<ExamResultDto>

    @GET("students/{careerId}/careers/{registrationId}/exams")
    suspend fun getExamDates(): List<ExamDateDto>

    @GET("students/{registrationId}/transcript")
    suspend fun getTranscript(): List<ExamGradeDto>

    @PUT("students/{registrationId}/exams/board/{gradeId}/acknowledgement")
    suspend fun changeGradeStatus(
        @Path("gradeId") gradeId: String,
        @Query("presaVisione") gradeStatus: String,
    ): ExamResultDto

    @POST("https://unito.prod.up.cineca.it/api/Impegni/getImpegniCalendarioPubblico")
    suspend fun getPublicAgenda(@Body body: PublicAgendaRequest): List<AgendaEntryDto>
}
