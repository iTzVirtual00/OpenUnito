// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

    id("co.uzzu.dotenv.gradle") version "4.0.0"
    id("com.google.devtools.ksp") version "2.3.6" apply false
    id("androidx.room3") version "3.0.3" apply false
}