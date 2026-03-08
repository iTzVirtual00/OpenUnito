package me.itzvirtual.myunitox.data.examdate.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.itzvirtual.myunitox.core.network.MicrosoftDateSerializer
import java.util.Date

@Serializable
data class ExamDateDto(
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("note") val note: String = "",
    @SerialName("ad_cod") val adCode: String = "",
    @SerialName("ad_des") val adDescription: String = "",
    @SerialName("app_des") val appDescription: String = "",

    @Serializable(with = MicrosoftDateSerializer::class)
    @SerialName("date") val date: Date,

    @Serializable(with = MicrosoftDateSerializer::class)
    @SerialName("bookingStartDate") val bookingStartDate: Date,

    @Serializable(with = MicrosoftDateSerializer::class)
    @SerialName("bookingEndDate") val bookingEndDate: Date,

    @SerialName("bookableExam") val bookableExam: Boolean = false,
    @SerialName("bookingEnabled") val bookingEnabled: Boolean = false,
    @SerialName("unbookingEnabled") val unbookingEnabled: Boolean = false,
    @SerialName("iscritti") val students: Int = 0,
    @SerialName("presidente") val president: String = "",
)
