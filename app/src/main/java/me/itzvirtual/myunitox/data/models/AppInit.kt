package me.itzvirtual.myunitox.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.itzvirtual.myunitox.data.remote.MicrosoftDateSerializer
import java.util.Date

@Serializable
data class StudentCareer(
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,

    @Serializable(with = MicrosoftDateSerializer::class)
    @SerialName("date_start")
    val dateStart: Date,
//    @SerialName("date_end")
//    val dateEnd: String?,
    @SerialName("registration_number")
    val studentId: String, // matricola
    @SerialName("registrationId")
    val registrationId: Int,
    @SerialName("notes")
    val notes: String,

//    @SerialName("is_blocked")
//    val isBlocked: Boolean,
    @SerialName("ordinamento")
    val regulationYear: String,
    @SerialName("normativa")
    val normativa: String,
    @SerialName("classe")
    val className: String,
    @SerialName("coorte")
    val cohort: String,

    @SerialName("percorsoDes")
    val percorsoDes: String,
    @SerialName("sedeDes")
    val sedeDes: String,
    @SerialName("iscrDes")
    val iscrDes: String,
    @SerialName("staStuDes")
    val staStuDes: String,

    @SerialName("annoCorso")
    val courseYear: Int
)

@Serializable
data class AuthSession(
    @SerialName("user_id")
    val userId: Int,
    @SerialName("username")
    val username: String,
    @SerialName("firstname")
    val firstname: String,
    @SerialName("lastname")
    val lastname: String,

    @SerialName("careers")
    val careers: List<StudentCareer>,

    @SerialName("type")
    val type: Int, // ?
)
typealias StudentData = AuthSession

@Serializable
data class AppInit(
    @SerialName("auth_session")
    val session: AuthSession,
)