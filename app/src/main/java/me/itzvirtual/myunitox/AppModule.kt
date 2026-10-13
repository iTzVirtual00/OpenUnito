package me.itzvirtual.myunitox

import me.itzvirtual.myunitox.core.database.DatabaseModule
import me.itzvirtual.myunitox.core.network.CoroutineScopesModule
import me.itzvirtual.myunitox.core.network.DispatchersModule
import me.itzvirtual.myunitox.core.network.NetworkModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(
    includes = [
        DatabaseModule::class,
        NetworkModule::class,
        CoroutineScopesModule::class,
        DispatchersModule::class,
    ]
)
@ComponentScan("me.itzvirtual.myunitox")
class AppModule
