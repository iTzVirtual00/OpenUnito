package me.itzvirtual.myunitox.core.network

import android.content.Context
import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.data.config.local.AppConfigDao
import me.itzvirtual.myunitox.data.student.StudentRepository
import me.itzvirtual.myunitox.data.student.local.StudentDao
import me.itzvirtual.myunitox.ui.shared.LocaleHelper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

const val API_URL = "https://unito.app.cineca.it/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideCurrentLanguage(@ApplicationContext context: Context): String =
        LocaleHelper.getLocale(context).language

    @Provides
    @Singleton
    fun provideOkHttpClient(
        currentLanguage: String,
        appConfigDao: AppConfigDao,
        @ApplicationContext context: Context,
    ): OkHttpClient {
        val logger = HttpLoggingInterceptor.Logger { message -> Log.i("UnitoAPI", message) }
        val httpLogger = HttpLoggingInterceptor(logger).apply {
            level = HttpLoggingInterceptor.Level.BODY
            redactHeader("Authorization")
            redactHeader("Cookie")
        }
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = appConfigDao.getTokenBlocking() // good idea?
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

    @Provides
    @Singleton
    fun provideStudentRepository(
        client: OkHttpClient,
        json: Json,
        studentDao: StudentDao,
    ): StudentRepository = StudentRepository(client, json).also { it.studentDao = studentDao }

    @Provides
    @Singleton
    fun provideUnitoApi(
        client: OkHttpClient,
        json: Json,
        studentRepository: StudentRepository,
    ): UnitoApi {
        val unitoClient = client.newBuilder()
            .addInterceptor { chain ->
                // inject studentId and careerId
                val originalRequest = chain.request()
                val originalUrl = originalRequest.url
                val career = runBlocking { studentRepository.getCurrentCareer() }
                val newUrl = originalUrl.newBuilder().apply {
                    val pathSegments = originalUrl.pathSegments
                    for (i in pathSegments.indices) {
                        when (pathSegments[i]) {
                            "{careerId}" -> setPathSegment(i, career.id.toString())
                            "{registrationId}" -> setPathSegment(
                                i,
                                career.registrationId.toString()
                            )
                        }
                    }
                }.build()
                chain.proceed(originalRequest.newBuilder().url(newUrl).build())
            }
            .ensureLoggingAtEnd()
            .build()

        return Retrofit.Builder()
            .baseUrl(API_URL)
            .client(unitoClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(UnitoApi::class.java)
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
