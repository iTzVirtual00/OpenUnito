package me.itzvirtual.openunito.core.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.annotation.Single

@Single(binds = [NetworkMonitor::class])
class WasmNetworkMonitor : NetworkMonitor {
    override val isOnline: Flow<Boolean> = flowOf(true)
}
