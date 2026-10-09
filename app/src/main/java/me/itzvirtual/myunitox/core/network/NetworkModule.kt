package me.itzvirtual.myunitox.core.network

import android.content.Context
import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.data.config.local.AppConfigDao
import me.itzvirtual.myunitox.data.student.StudentRepository
import me.itzvirtual.myunitox.ui.shared.LocaleHelper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

const val API_URL = "https://unito.app.cineca.it/"
private val careerInitMutex = Mutex()

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
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideUnitoApi(
        client: OkHttpClient,
        json: Json,
        studentRepository: Lazy<StudentRepository>,
    ): UnitoApi {
        val unitoClient = client.newBuilder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val originalUrl = originalRequest.url

                val career = runBlocking {
                    careerInitMutex.withLock {
                        studentRepository.get().getCurrentCareer()
                    }
                }

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

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopesModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @Provides
    @Singleton
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

