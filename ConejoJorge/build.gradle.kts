// Top-level build file where you can add configuration options common to all sub-projects/modules.
// AGP 9 trae el soporte de Kotlin integrado: ya no hace falta el plugin kotlin-android.
plugins {
    alias(libs.plugins.android.application) apply false
}
