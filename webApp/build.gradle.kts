import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.internal.platform.wasm.WasmPlatforms.wasmJs

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlin.compose)
}


kotlin {
//    js {
//        browser()
//        binaries.executable()
//    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        useEsModules()
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":shared"))

            implementation(libs.compose.ui)
            implementation(libs.koin.core)
            implementation(
                npm("sqlite-wasm-worker", layout.projectDirectory.dir("sqlite-wasm-worker").asFile)
            )
        }
    }
}

