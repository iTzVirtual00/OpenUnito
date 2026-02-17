package me.itzvirtual.myunitox.data.remote

import android.content.Context
import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.ui.LocaleHelper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Serializable
data class StudentCareer(
    @SerialName("id")
    val studentId: Int,
//    @SerialName("title")
//    val title: String,
//    @SerialName("description")
//    val description: String,
//    @SerialName("date_start")
//    val dateStart: String,
//    @SerialName("date_end")
//    val dateEnd: String?,
//    @SerialName("registration_number")
//    val registrationNumber: String,
    @SerialName("registrationId")
    val careerId: Int,
//    @SerialName("notes")
//    val notes: String,

//    @SerialName("is_blocked")
//    val isBlocked: Boolean,
//    @SerialName("ordinamento")
//    val ordinamento: String,
//    @SerialName("normativa")
//    val normativa: String,
//    @SerialName("classe")
//    val classe: String,
//    @SerialName("coorte")
//    val coorte: String,
//    @SerialName("percorsoDes")
//    val percorsoDes: String,
//    @SerialName("sedeDes")
//    val sedeDes: String,
//    @SerialName("iscrDes")
//    val iscrDes: String,
//    @SerialName("staStuDes")
//    val staStuDes: String,
//    @SerialName("annoCorso")
//    val annoCorso: Int

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
    val type: Int,

    )

@Serializable
data class AppInit(
    @SerialName("auth_session")
    val session: AuthSession,
)

@Serializable
data class StudentData(
    val studentId: String,
    val careerId: String,
)

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // TODO: Language change needs a restart because of this!
    @Provides
    @Singleton
    fun provideCurrentLanguage(@ApplicationContext context: Context): String {
        // We use the ApplicationContext here because it's safe to keep in a Singleton
        return LocaleHelper.getLocale(context).language
    }

    @Provides
    @Singleton
    fun provideAuthStorage(@ApplicationContext context: Context): AuthStorage {
        return AuthStorage(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        currentLanguage: String,
        authStorage: AuthStorage,
        @ApplicationContext context: Context
    ): OkHttpClient {
        val logger = HttpLoggingInterceptor.Logger { message ->
            Log.i("UnitoAPI", message)
        }

        val httpLogger = HttpLoggingInterceptor(logger).apply {
            level = HttpLoggingInterceptor.Level.BODY
            redactHeader("Authorization")
            redactHeader("Cookie")
        }

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = authStorage.getToken()
                val request = chain.request().newBuilder()
                    .addHeader("ResultLang", currentLanguage)
                    .addHeader("Authorization", token ?: "")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(httpLogger)
            .build()
    }

    @Provides
    @Singleton
    fun provideJson() = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Volatile
    private var cachedStudentData: StudentData? = null

    @Provides
    @Singleton
    fun provideUnitoAPI(client: OkHttpClient, json: Json): UnitoAPI {
        val unitoClient = client.newBuilder()
            .addInterceptor { chain ->
                // inject studentId and careerId
                val originalRequest = chain.request()
                val originalUrl = originalRequest.url

                val studentData = synchronized(this) {
                    cachedStudentData ?: getStudentData(client, json).also {
                        cachedStudentData = it
                    }
                }

                val newUrl = originalUrl.newBuilder()
                    .apply {
                        val pathSegments = originalUrl.pathSegments
                        for (i in pathSegments.indices) {
                            when (pathSegments[i]) {
                                "{studentId}" -> setPathSegment(i, studentData.studentId)
                                "{careerId}" -> setPathSegment(i, studentData.careerId)
                            }
                        }
                    }
                    .build()

                val newRequest = originalRequest.newBuilder()
                    .url(newUrl)
                    .build()

                chain.proceed(newRequest)
            }
            .ensureLoggingAtEnd()
            .build()

        return Retrofit.Builder()
            .baseUrl("https://unito.app.cineca.it/")
            .client(unitoClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(UnitoAPI::class.java)
    }
}

// 3. Updated helper function to accept json (don't call provideJson() repeatedly)
fun getStudentData(client: OkHttpClient, json: Json): StudentData {
    val request = okhttp3.Request.Builder()
        .url("https://unito.app.cineca.it/app/init?os=android&os_version=16&app_version=25.7.0.0.17")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw Exception("AppInit failed")
        val body = response.body.string()
        val appInit = json.decodeFromString<AppInit>(body)
        val career = appInit.session.careers.first()
        return StudentData(
            studentId = career.studentId.toString(),
            careerId = career.careerId.toString()
        )
    }
}

fun OkHttpClient.Builder.ensureLoggingAtEnd(): OkHttpClient.Builder = apply {
    val currentInterceptors = interceptors()
    val logger = currentInterceptors.find { it is HttpLoggingInterceptor }

    if (logger != null) {
        currentInterceptors.remove(logger)
        currentInterceptors.add(logger)
    }
}