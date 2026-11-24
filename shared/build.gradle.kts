import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.INT
import java.io.FileInputStream
import java.util.Properties
import kotlin.apply

val versionProps = Properties().apply {
    val versionPropsFile = file(rootProject.file("version.properties"))
    if (versionPropsFile.exists()) {
        load(FileInputStream(versionPropsFile))
    }
}
val appVersionCode: Int = versionProps.getProperty("versionCode")?.toIntOrNull() ?: 1
val appVersionName: String = versionProps.getProperty("versionName") ?: "v0.0.1"

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)

    alias(libs.plugins.ksp)

    alias(libs.plugins.androidx.room3)

    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlin.compose) apply true
    alias(libs.plugins.kotlin.serialization)

    alias(libs.plugins.buildkonfig)
}

compose.resources {
    packageOfResClass = "me.itzvirtual.openunito.resources"
    generateResClass = always
}

buildkonfig {
    packageName = "me.itzvirtual.openunito"
    defaultConfigs {
        buildConfigField(STRING, "VERSION_NAME", appVersionName)
        buildConfigField(INT, "VERSION_CODE", appVersionCode.toString())
    }
}

kotlin {
    room3 {
        schemaDirectory("$projectDir/schemas")
    }
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlinx.coroutines.ExperimentalCoroutinesApi")
    }

    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    android {
        namespace = "me.itzvirtual.openunito"
        compileSdk {
            version = release(37)
        }
        minSdk = 24

        androidResources {
            enable = true
        }

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
//    val xcfName = "sharedKit"

//    iosX64 {
//        binaries.framework {
//            baseName = xcfName
//        }
//    }
//
//    iosArm64 {
//        binaries.framework {
//            baseName = xcfName
//        }
//    }
//
//    iosSimulatorArm64 {
//        binaries.framework {
//            baseName = xcfName
//        }
//    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                // Add KMP dependencies herewe
                implementation(libs.compose.navigationevent)
                implementation(libs.navigation.compose)
                implementation(libs.material3.adaptive.navigation.suite)

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.koin.annotations)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)

                implementation(libs.kotlinx.serialization.json)

                implementation(libs.androidx.room3.runtime)
                implementation(libs.androidx.sqlite.async)

                implementation(libs.touchlab.kermit)

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)

                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.compose.components.resources)

                implementation(libs.material.icons.extended)

                implementation(libs.atomicfu)

                implementation(libs.locale.core)
                implementation(libs.locale.platform)
                implementation(libs.locale.datetime.cldr.skeletons)

                implementation(libs.okio)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.koin.test)
            }
        }

        androidMain {
            dependencies {
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
                implementation(libs.ktor.client.okhttp)

                implementation(libs.androidx.core.ktx)
                implementation(libs.compose.uiTooling)
                implementation(libs.koin.android)

                implementation(libs.androidx.core.splashscreen)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.core)
                implementation(libs.androidx.junit)
                implementation(libs.androidx.runner)
            }
        }

        wasmJsMain {
            dependencies {
                // wasmjs specific libraries
                implementation(libs.ktor.client.js)
                api(libs.androidx.sqlite.web)
            }
        }

//        iosMain {
//            dependencies {
//                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
//                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
//                // part of KMP’s default source set hierarchy. Note that this source set depends
//                // on common by default and will correctly pull the iOS artifacts of any
//                // KMP dependencies declared in commonMain.
//            }
//        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
    add("kspAndroid", libs.koin.ksp.compiler)
    add("kspWasmJs", libs.koin.ksp.compiler)

    add("kspAndroid", libs.androidx.room3.compiler)
    add("kspWasmJs", libs.androidx.room3.compiler)
}

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
}

// TODO: Verify this AI fix:
configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "io.insert-koin" && requested.name.startsWith("koin-annotations")) {
            useVersion(libs.versions.koinAnnotations.get())
            because("koin-ksp-compiler requires matching koin-annotations")
        }
    }
}