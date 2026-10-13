package me.itzvirtual.myunitox.core.network

import android.content.Context
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.data.config.AppConfigRepository
import me.itzvirtual.myunitox.data.student.StudentRepository
import me.itzvirtual.myunitox.ui.shared.LocaleHelper
import org.koin.core.annotation.Module
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Single

const val API_URL = "https://unito.app.cineca.it/"

@Module
class NetworkModule {
    @Single
    fun provideCurrentLanguage(context: Context): String =
        LocaleHelper.getLocale(context).language

    @Single
    fun provideHttpClient(
        currentLanguage: String,
        appConfigRepo: AppConfigRepository,
        json: Json,
    ): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.i("UnitoAPI", message)
                }
            }
            level = LogLevel.BODY
        }
        defaultRequest {
            url(API_URL)
            header("ResultLang", currentLanguage)
            appConfigRepo.getTokenInMemory()?.let { token ->
                if (token.isNotBlank()) {
                    header("Authorization", token)
                }
            }
        }
    }

    @Single
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Single
    fun provideUnitoApi(
        client: HttpClient,
        studentRepository: StudentRepository,
    ): UnitoApi = UnitoApi(client, studentRepository)
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope

@Module
class CoroutineScopesModule {

    @Single
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
class DispatchersModule {

    @Single
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
