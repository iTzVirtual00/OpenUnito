package me.itzvirtual.openunito

import me.itzvirtual.openunito.core.database.DatabaseModule
import me.itzvirtual.openunito.core.network.CoroutineScopesModule
import me.itzvirtual.openunito.core.network.DispatchersModule
import me.itzvirtual.openunito.core.network.NetworkModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(
    includes = [
        PlatformModule::class,
        DatabaseModule::class,
        NetworkModule::class,
        CoroutineScopesModule::class,
        DispatchersModule::class,
    ]
)
@ComponentScan("me.itzvirtual.openunito")
class AppModule