package me.itzvirtual.myunitox.data.remote

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.ui.LocaleHelper
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import javax.inject.Singleton
import kotlin.jvm.java
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val HARDCODED_TOKEN = "REDACTED_BASIC_TOKEN"
    private const val HARDCODED_COOKIE = "REDACTED_COOKIE"

    // TODO: Language change needs a restart because of this!
    @Provides
    @Singleton
    fun provideCurrentLanguage(@ApplicationContext context: Context): String {
        // We use the ApplicationContext here because it's safe to keep in a Singleton
        return LocaleHelper.getLocale(context).language
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        currentLanguage: String
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
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", HARDCODED_TOKEN)
                    .addHeader("Cookie", HARDCODED_COOKIE)
                    .addHeader("ResultLang", currentLanguage)
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
    fun provideUnitoAPI(client: OkHttpClient): UnitoAPI {
        val json = provideJson()

        return Retrofit.Builder()
            .baseUrl("https://unito.app.cineca.it/")
            .client(client)
            // 2. Use the Kotlinx Serialization Converter instead of Gson
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(UnitoAPI::class.java)
    }
}