package me.itzvirtual.myunitox.data.remote

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
import me.itzvirtual.myunitox.data.local.SharedPrefStorage
import me.itzvirtual.myunitox.data.repository.StudentDataRepository
import me.itzvirtual.myunitox.ui.LocaleHelper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

const val API_URL = "https://unito.app.cineca.it/"
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
    fun provideAuthStorage(@ApplicationContext context: Context): SharedPrefStorage {
        return SharedPrefStorage(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        currentLanguage: String,
        sharedPrefStorage: SharedPrefStorage,
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
                val token = sharedPrefStorage.getToken()
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
    fun provideStudentDataRepository(client: OkHttpClient, json: Json): StudentDataRepository {
        return StudentDataRepository(client, json)
    }

    @Provides
    @Singleton
    fun provideUnitoAPI(
        client: OkHttpClient,
        json: Json,
        studentDataRepository: StudentDataRepository
    ): UnitoAPI {
        val unitoClient = client.newBuilder()
            .addInterceptor { chain ->
                // inject studentId and careerId
                val originalRequest = chain.request()
                val originalUrl = originalRequest.url

                // Use cached data from repository, or fetch synchronously if not available
                val career = runBlocking {studentDataRepository.getCurrentCareer()}

                val newUrl = originalUrl.newBuilder()
                    .apply {
                        val pathSegments = originalUrl.pathSegments
                        for (i in pathSegments.indices) {
                            when (pathSegments[i]) {
                                "{careerId}" -> setPathSegment(i, career.id.toString())
                                "{registrationId}" -> setPathSegment(i, career.registrationId.toString())
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
            .baseUrl(API_URL)
            .client(unitoClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(UnitoAPI::class.java)
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