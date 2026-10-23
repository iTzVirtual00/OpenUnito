package me.itzvirtual.openunito.core.network

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger as KtorLogger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import me.itzvirtual.openunito.data.config.AppConfigRepository
import me.itzvirtual.openunito.data.student.StudentRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Single

expect val API_URL: String

expect fun getSystemLanguage(): String

fun Throwable.isNetworkError(): Boolean =
    this is HttpRequestTimeoutException ||
            this is ConnectTimeoutException ||
            this is SocketTimeoutException ||
            this is IOException

@Module
class NetworkModule {
    @Single
    fun provideCurrentLanguage(): String = getSystemLanguage()

    @Single
    fun provideHttpClient(
        currentLanguage: String,
        appConfigRepo: AppConfigRepository,
        json: Json,
    ): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            logger = object : KtorLogger {
                override fun log(message: String) {
                    Logger.i(tag = "UnitoAPI") { message }
                }
            }
            level = LogLevel.BODY
        }
        defaultRequest {
            url(API_URL)
            header("ResultLang", currentLanguage)
            appConfigRepo.getToken()?.let { token ->
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
    @Named("ApplicationScope")
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

expect val platformIoDispatcher: CoroutineDispatcher

@Module
class DispatchersModule {

    @Single
    @Named("IoDispatcher")
    fun provideIoDispatcher(): CoroutineDispatcher = platformIoDispatcher

    @Single
    @Named("DefaultDispatcher")
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Single
    @Named("MainDispatcher")
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}
