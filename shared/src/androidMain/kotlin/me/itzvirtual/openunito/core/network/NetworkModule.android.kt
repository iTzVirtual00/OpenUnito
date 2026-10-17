package me.itzvirtual.openunito.core.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.util.Locale

actual fun getSystemLanguage(): String = Locale.getDefault().language
actual val platformIoDispatcher: CoroutineDispatcher = Dispatchers.IO