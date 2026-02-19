package me.itzvirtual.myunitox.data.remote

import me.itzvirtual.myunitox.data.models.AgendaEntry
import me.itzvirtual.myunitox.data.models.DashboardDataResponse
import me.itzvirtual.myunitox.data.models.ExamDate
import me.itzvirtual.myunitox.data.models.ExamGrade
import me.itzvirtual.myunitox.data.models.ExamResult
import me.itzvirtual.myunitox.data.models.PublicAgendaRequest
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UnitoAPI {
//    @GET("app/init")
//    suspend fun getAppInit(
//        @Query("os") os: String = "android",
//        @Query("os_version") osVersion: String,
//        @Query("app_version") appVersion: String
//    ): AppInitResponse
//
//    @GET("inbox")
//    suspend fun getInbox(): InboxResponse

    @GET("students/{studentId}/careers/{careerId}/dashboard")
    suspend fun getDashboardData(): DashboardDataResponse


    @GET("students/{careerId}/exams/board")
    suspend fun getExamBoard(): List<ExamResult>

    @GET("students/{studentId}/careers/{careerId}/exams")
    suspend fun getExamDates(): List<ExamDate>

    @GET("students/{careerId}/transcript")
    suspend fun getTranscript(): List<ExamGrade>

    @PUT("students/{careerId}/exams/board/{gradeId}/acknowledgement")
    suspend fun changeGradeStatus(
        @Path("gradeId") gradeId: String,
        @Query("presaVisione") gradeStatus: String
    ): ExamResult

    @POST("https://unito.prod.up.cineca.it/api/Impegni/getImpegniCalendarioPubblico")
    suspend fun getPublicAgenda(@Body body: PublicAgendaRequest): List<AgendaEntry>
}
