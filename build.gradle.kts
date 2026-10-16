// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

    id("co.uzzu.dotenv.gradle") version "4.0.0"
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room3) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.android.lint) apply false

    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false

//    alias(libs.plugins.koin.compiler) apply false
}