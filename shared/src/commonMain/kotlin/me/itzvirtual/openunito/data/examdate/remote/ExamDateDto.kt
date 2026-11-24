package me.itzvirtual.openunito.data.examdate.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import me.itzvirtual.openunito.core.network.MicrosoftDateSerializer
import kotlin.time.Instant

@Serializable
data class ExamDateDto(
    @SerialName("adsce_id") val adsceId: Long = 0L,
    @SerialName("ad_id") val adId: Long = 0L,
    @SerialName("cod") val cod: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("note") val note: String? = "",

    @Serializable(with = MicrosoftDateSerializer::class)
    @SerialName("date") val date: Instant,

    @Serializable(with = MicrosoftDateSerializer::class)
    @SerialName("bookingStartDate") val bookingStartDate: Instant,

    @Serializable(with = MicrosoftDateSerializer::class)
    @SerialName("bookingEndDate") val bookingEndDate: Instant,

    @SerialName("bookableExam") val bookableExam: Boolean = false,
    @SerialName("bookingEnabled") val bookingEnabled: Boolean = false,
    @SerialName("unbookingEnabled") val unbookingEnabled: Boolean = false,
    @SerialName("cds_app_id") val cdsAppId: Long = 0L,
    @SerialName("ad_app_id") val adAppId: Long = 0L,
    @SerialName("iscritti") val students: Int = 0,
    @SerialName("posiz") val position: String? = null,
    @SerialName("adreg_id") val adregId: Long? = null,
    @SerialName("applista_id") val applistaId: Long? = null,
    @SerialName("app_id") val appId: Long = 0L,
    @SerialName("tipo_app_cod") val tipoAppCod: String = "",
    @SerialName("ad_cod") val adCode: String = "",
    @SerialName("ad_des") val adDescription: String = "",
    @SerialName("app_des") val appDescription: String = "",
    @SerialName("presidente") val president: String = "",
    @SerialName("tipo_des") val tipoDes: String? = null,
    @SerialName("stato_quest") val statoQuest: Int = 0,
    @SerialName("des_edificio") val desEdificio: String? = null,
    @SerialName("des_aula") val desAula: String? = null,
    @SerialName("dom_part_cod") val domPartCod: String? = null,
    @SerialName("fat_part_cod") val fatPartCod: String? = null,
    @SerialName("turni") val turni: String? = null,
    @SerialName("sessioni") val sessioni: JsonElement? = null,
    @SerialName("misureComp") val misureComp: String? = null,
    @SerialName("tipiSvolgimentoEsame") val tipiSvolgimentoEsame: String? = null,
    @SerialName("chkQuestVal") val chkQuestVal: String = "0",
    @SerialName("visCancPren") val visCancPren: Boolean = false
)

@Serializable
data class BookExamRequest(
    @SerialName("misureComp") val misureComp: Boolean = false,
    @SerialName("tipoSvolgimentoEsame") val tipoSvolgimentoEsame: Nothing? = null,
)

@Serializable
data class ExamBookingResponse(
    @SerialName("is_successfull") val isSuccessfull: Boolean = false,
//    @SerialName("message") val message: String? = null // not tested!
)

