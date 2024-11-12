// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application").version("8.0.1") apply false
    id("com.android.library").version("8.0.1") apply false
    id("org.jetbrains.kotlin.android").version("1.9.20") apply false
    id("com.google.devtools.ksp").version("1.9.20-1.0.14") apply false

    id("com.google.dagger.hilt.android") version "2.48" apply false
    kotlin("plugin.serialization") version "1.9.20"
    id("com.google.firebase.appdistribution") version "4.0.0" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
}