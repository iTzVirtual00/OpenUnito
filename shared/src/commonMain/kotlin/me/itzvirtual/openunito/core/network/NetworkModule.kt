package me.itzvirtual.openunito.core.network

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import me.itzvirtual.openunito.data.localization.AppLocaleManager
import me.itzvirtual.openunito.data.config.AppConfigRepository
import me.itzvirtual.openunito.data.student.StudentRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Single
import io.ktor.client.plugins.logging.Logger as KtorLogger

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
    fun provideHttpClient(
        appConfigRepo: AppConfigRepository,
        appLocaleManager: AppLocaleManager,
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
        install(createClientPlugin("InjectAppHeaders") {
            onRequest { request, _ ->
                // TODO: invalidate cache on apiLanguage change
                request.headers["ResultLang"] = appLocaleManager.awaitLocale().apiLanguage
                request.headers["Authorization"] = appConfigRepo.tokenState.filterNotNull().first()
            }
        })
        defaultRequest {
            url(API_URL)
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
    fun provideApplicationScope(
        @Named("DefaultDispatcher") defaultDispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(SupervisorJob() + defaultDispatcher)
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
