plugins {
    id("com.android.application") version "9.1.0" apply false

    // Add the dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.4" apply false
    alias(libs.plugins.kotlin.compose) apply false
}
