package me.itzvirtual.myunitox.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.itzvirtual.myunitox.data.remote.InstantIsoSerializer
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Serializable
data class AgendaEntry(
    @SerialName("id") val id: String,
    @SerialName("nome") val title: String,
    @SerialName("nome_EN") val titleEn: String? = null,

    @SerialName("dataInizio")
    @Serializable(with = InstantIsoSerializer::class)
    val startDateTime: Instant,

    @SerialName("dataFine")
    @Serializable(with = InstantIsoSerializer::class)
    val endDateTime: Instant,

    @SerialName("codiceAttivita") val activityCode: String,
    @SerialName("annoCorso") val courseYear: Int,
    @SerialName("stato") val status: String, // E.g., "P"

    @SerialName("aule") val rooms: List<Room> = emptyList(),
    @SerialName("docenti") val professors: List<Professor> = emptyList(),
    @SerialName("tipoAttivita") val activityType: ActivityType,

    @SerialName("teledidattica") val isRemote: Boolean = false,
    @SerialName("indisponibilita") val isUnavailable: Boolean = false
) {
    fun isToday(): Boolean {
        val today = LocalDate.now()
        val entryDate = startDateTime.atZone(ZoneId.systemDefault()).toLocalDate()
        return entryDate == today
    }
}

@Serializable
data class Room(
    @SerialName("id") val id: String,
    @SerialName("codice") val code: String,
    @SerialName("descrizione") val name: String,
    @SerialName("capienza") val capacity: Int? = null,
    @SerialName("fotoUrl") val photoUrl: String? = null,
    @SerialName("planimetriaUrl") val floorPlanUrl: String? = null,
    @SerialName("edificio") val building: Building? = null,
    @SerialName("piano") val floor: Floor? = null
)

@Serializable
data class Building(
    @SerialName("id") val id: String,
    @SerialName("descrizione") val name: String,
    @SerialName("via") val address: String? = null,
    @SerialName("numeroCivico") val streetNumber: String? = null,
    @SerialName("comune") val city: String? = null,
    @SerialName("geo") val coordinates: Coordinates? = null
)

@Serializable
data class Professor(
    @SerialName("id") val id: String,
    @SerialName("nome") val firstName: String,
    @SerialName("cognome") val lastName: String
) {
    val fullName: String get() = "$firstName $lastName"
}

@Serializable
data class ActivityType(
    @SerialName("codice") val code: String, // E.g., "LEZ"
    @SerialName("descrizione") val description: String // E.g., "Lezione"
)

@Serializable
data class Floor(
    @SerialName("codice") val code: String,
    @SerialName("descrizione") val description: String
)

@Serializable
data class Coordinates(
    @SerialName("lat") val latitude: Double,
    @SerialName("lng") val longitude: Double
)

@Serializable
data class ActivityDetail(
    @SerialName("cfu") val credits: Int? = null,
    @SerialName("totaleOre") val totalHours: Int? = null,
    @SerialName("tipoInsegnamento") val teachingType: String? = null
)

@Serializable
data class PublicAgendaRequest(
    @SerialName("linkCalendarioId")
    val calendarLinkId: String,// = "613b9237d969e100173d4110",

    @SerialName("clienteId")
    val clientId: String,// = "5852fd1ab7305612a8354d51",

    @SerialName("dataInizio")
    val startDate: String,// = "2026-02-15T23:00:00.000Z",

    @SerialName("dataFine")
    val endDate: String,// = "2026-02-20T23:00:00.000Z",

    @SerialName("mostraImpegniAnnullati")
    val showCancelledEvents: Boolean = true,

    @SerialName("mostraIndisponibilitaTotali")
    val showFullUnavailability: Boolean = false,

    @SerialName("filtroSfondoId")
    val backgroundFilterId: String? = null,

    @SerialName("pianificazioneTemplate")
    val isTemplatePlanning: Boolean = false
)