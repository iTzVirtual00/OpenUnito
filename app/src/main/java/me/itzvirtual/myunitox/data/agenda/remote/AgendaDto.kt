package me.itzvirtual.myunitox.data.agenda.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.itzvirtual.myunitox.core.network.InstantIsoSerializer
import java.time.Instant

@Serializable
data class AgendaEntryDto(
    @SerialName("id") val id: String,
    @SerialName("nome") val title: String,
    @SerialName("nome_EN") val titleEn: String? = null,

    @SerialName("dataInizio")
    @Serializable(with = InstantIsoSerializer::class)
    val startDateTime: Instant,

    @SerialName("dataFine")
    @Serializable(with = InstantIsoSerializer::class)
    val endDateTime: Instant,

    @SerialName("codiceAttivita") val activityCode: String = "",
    @SerialName("annoCorso") val courseYear: Int,
    @SerialName("stato") val status: String, // E.g., "P"
    @SerialName("aule") val rooms: List<RoomLocationDto> = emptyList(),
    @SerialName("docenti") val professors: List<ProfessorDto> = emptyList(),
    @SerialName("tipoAttivita") val activityType: ActivityTypeDto,
    @SerialName("teledidattica") val isRemote: Boolean = false,
    @SerialName("indisponibilita") val isUnavailable: Boolean = false,
)

@Serializable
data class RoomLocationDto(
    @SerialName("id") val id: String,
    @SerialName("codice") val code: String,
    @SerialName("descrizione") val name: String,
    @SerialName("capienza") val capacity: Int? = null,
    @SerialName("fotoUrl") val photoUrl: String? = null,
    @SerialName("planimetriaUrl") val floorPlanUrl: String? = null,
    @SerialName("edificio") val building: BuildingDto? = null,
    @SerialName("piano") val floor: FloorDto? = null,
)

@Serializable
data class BuildingDto(
    @SerialName("id") val id: String,
    @SerialName("descrizione") val name: String,
    @SerialName("via") val address: String? = null,
    @SerialName("numeroCivico") val streetNumber: String? = null,
    @SerialName("comune") val city: String? = null,
    @SerialName("geo") val coordinates: CoordinatesDto? = null,
)

@Serializable
data class ProfessorDto(
    @SerialName("id") val id: String,
    @SerialName("nome") val firstName: String,
    @SerialName("cognome") val lastName: String,
)

@Serializable
data class ActivityTypeDto(
    @SerialName("codice") val code: String,
    @SerialName("descrizione") val description: String,
)

@Serializable
data class FloorDto(
    @SerialName("codice") val code: String,
    @SerialName("descrizione") val description: String,
)

@Serializable
data class CoordinatesDto(
    @SerialName("lat") val latitude: Double,
    @SerialName("lng") val longitude: Double,
)

@Serializable
data class PublicAgendaRequest(
    @SerialName("linkCalendarioId") val calendarLinkId: String,
    @SerialName("clienteId") val clientId: String,
    @SerialName("dataInizio") val startDate: String,
    @SerialName("dataFine") val endDate: String,
    @SerialName("mostraImpegniAnnullati") val showCancelledEvents: Boolean = true,
    @SerialName("mostraIndisponibilitaTotali") val showFullUnavailability: Boolean = false,
    @SerialName("filtroSfondoId") val backgroundFilterId: String? = null,
    @SerialName("pianificazioneTemplate") val isTemplatePlanning: Boolean = false,
)
