package me.itzvirtual.openunito.data.student.remote

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.itzvirtual.openunito.core.network.MicrosoftDateSerializer
import kotlin.time.Instant

@Serializable
data class StudentCareerDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,

    @Serializable(with = MicrosoftDateSerializer::class)
    @SerialName("date_start") val dateStart: Instant,

    @SerialName("date_end") val dateEnd: String?,
    @SerialName("registration_number") val studentId: String,
    @SerialName("registrationId") val registrationId: Int,
    @SerialName("notes") val notes: String,
    @SerialName("ordinamento") val regulationYear: String,
    @SerialName("normativa") val normativa: String,
    @SerialName("classe") val className: String,
    @SerialName("coorte") val cohort: String,
    @SerialName("percorsoDes") val percorsoDes: String,
    @SerialName("sedeDes") val sedeDes: String,
    @SerialName("iscrDes") val iscrDes: String,
    @SerialName("staStuDes") val staStuDes: String,
    @SerialName("annoCorso") val courseYear: Int,
)

@Serializable
data class AuthSessionDto(
    @SerialName("user_id") val userId: Int,
    @SerialName("username") val username: String,
    @SerialName("firstname") val firstname: String,
    @SerialName("lastname") val lastname: String,
    @SerialName("careers") val careers: List<StudentCareerDto>,
    @SerialName("type") val type: Int,
)

@Serializable
data class AppInitDto(
    @SerialName("auth_session") val session: AuthSessionDto?,
)

sealed class AppInitError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class HttpError(val status: HttpStatusCode) : AppInitError("/app/init returned $status")
    class Generic(message: String = "Generic error", cause: Throwable? = null) : AppInitError(message, cause)
}
