package me.itzvirtual.openunito

import org.koin.core.module.Module
import org.koin.ksp.generated.module

actual fun getPlatformAppModules(): List<Module> = listOf(
    AppModule().module,
    AndroidPlatformModule().module,
)