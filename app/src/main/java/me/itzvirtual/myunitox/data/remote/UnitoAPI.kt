package me.itzvirtual.myunitox.data.remote

import me.itzvirtual.myunitox.data.models.DashboardDataResponse
import me.itzvirtual.myunitox.data.models.ExamDate
import me.itzvirtual.myunitox.data.models.ExamResult
import retrofit2.http.GET
import retrofit2.http.Path

const val HARDCODED_STUDENTID = 1483920
const val HARDCODED_CARREERID = 1926481
const val LOCAL_RESPONSES = false
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
    suspend fun getDashboardData(
        @Path("studentId") studentId: String,
        @Path("careerId") careerId: String
    ): DashboardDataResponse



    @GET("students/{careerId}/exams/board")
    suspend fun getExamBoard(
        @Path("careerId") careerId: String
    ): List<ExamResult>

    @GET("students/{studentId}/careers/{careerId}/exams")
    suspend fun getExamDates(
        @Path("studentId") studentId: String,
        @Path("careerId") careerId: String
    ): List<ExamDate>
}
