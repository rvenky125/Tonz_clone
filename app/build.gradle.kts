import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("kotlinx-serialization")
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution")
    id("com.google.firebase.crashlytics")
}


android {
    namespace = "com.famas.tonz"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.famas.tonz"
        minSdk = 24
        targetSdk = 34
        versionCode = 27
        versionName = "1.0.42"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        val localProps = Properties()
        localProps.load(project.rootProject.file("local.properties").inputStream())
        val googleAuthClientId = localProps.getProperty("GOOGLE_AUTH_CLIENT_ID")
        val tonzAuthToken = localProps.getProperty("TONZ_API_TOKEN")
        val baseUrl = localProps.getProperty("BASE_URL" )

        buildConfigField("String", "GOOGLE_AUTH_CLIENT_ID", googleAuthClientId)
        buildConfigField("String", "TONZ_API_TOKEN", tonzAuthToken)
        buildConfigField("String", "BASE_URL", baseUrl)
    }

    applicationVariants.all {
        addJavaSourceFoldersToModel(
            File(buildDir, "generated/ksp/$name/kotlin")
        )
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            firebaseAppDistribution {
                releaseNotesFile="releasenotes.txt"
                testers="venkypaithireddy@gmail.com"
                artifactPath = "C:\\Users\\venky\\AndroidStudioProjects\\Tonz\\app\\release\\app-release.apk"
                appId = "1:943496621679:android:cb7f1cda06f3a5d7a7e79a"
            }
        }

        getByName("debug") {
//            firebaseAppDistribution {
//                releaseNotesFile="releasenotes.txt"
//                testers="prajesh90599@gmail.com, venkypaithireddy@gmail.com"
//                artifactPath = "C:\\Users\\venky\\AndroidStudioProjects\\Tonz\\app\\build\\outputs\\apk\\debug\\app-debug.apk"
//                appId = "1:943496621679:android:cb7f1cda06f3a5d7a7e79a"
//            }
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //UI
    val compose = "1.6.5"
    implementation("androidx.compose.ui:ui:$compose")
    implementation("androidx.compose.ui:ui-graphics:$compose")
    implementation("androidx.compose.ui:ui-tooling-preview:$compose")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material3:material3-window-size-class-android:1.2.1")
    implementation("androidx.compose.material:material-icons-core:$compose")
    implementation("androidx.compose.material:material-icons-extended:$compose")
    implementation("androidx.compose.runtime:runtime-livedata:$compose")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    //Coid
    implementation("io.coil-kt:coil-compose:2.5.0")

    //Accompanist
    val accompanist_version = "0.32.0"
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanist_version")
    implementation("com.google.accompanist:accompanist-webview:$accompanist_version")
    implementation("com.google.accompanist:accompanist-permissions:$accompanist_version")
    implementation("com.google.accompanist:accompanist-swiperefresh:$accompanist_version")

    //Test
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.5")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.5")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.5")

    //Destinations
    val destinationsVersion = "1.10.2"
    implementation("io.github.raamcosta.compose-destinations:animations-core:$destinationsVersion")
    ksp("io.github.raamcosta.compose-destinations:ksp:$destinationsVersion")

    //ViewModel
    val lifecycle_version = "2.7.0"
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    // Saved state module for ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycle_version")

    //Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    ksp("androidx.hilt:hilt-compiler:1.1.0")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("com.github.vestrel00:contacts-android:0.2.4")

    //Wave form
    val waveFormVersion = "1.1.1"
    implementation("com.github.lincollincol:compose-audiowaveform:$waveFormVersion")
    implementation("com.github.lincollincol:amplituda:2.2.2")

    /* Media3 */
    val media3_version = "1.3.1"
    implementation("androidx.media3:media3-exoplayer:$media3_version")
    implementation("androidx.media3:media3-session:$media3_version")
    implementation("androidx.media3:media3-ui:$media3_version")

    implementation("org.jodd:jodd-util:6.1.0")

    //FFMpeg
    implementation("com.arthenica:mobile-ffmpeg-audio:4.4")

    //Date Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    //Ktor
    val ktor_version = "2.3.9"
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    //Work manager
    val work_version = "2.9.0"
    implementation("androidx.work:work-runtime-ktx:$work_version")
    implementation("androidx.hilt:hilt-work:1.2.0")

    val lottieVersion = "6.0.0"
    implementation("com.airbnb.android:lottie-compose:$lottieVersion")


    // Import the BoM for the Firebase platform
    val firebaseBom = platform("com.google.firebase:firebase-bom:32.1.0")
    implementation(firebaseBom)

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth-ktx")

    // Also add the dependency for the Google Play services library and specify its version
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.firebase:firebase-config-ktx:22.0.0")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    implementation("com.github.stevdza-san:OneTapCompose:1.0.3")

    //Datastore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    //Paging
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("androidx.paging:paging-compose:3.2.1")

    //ROOM
//    implementation("androidx.room:room-ktx:2.5.2")
//    ksp("androidx.room:room-compiler:2.5.2")
//    implementation("androidx.room:room-paging:2.5.2")

    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("com.google.android.gms:play-services-ads:23.1.0")

    implementation("androidx.ads:ads-identifier:1.0.0-alpha05")
    implementation("com.google.guava:guava:28.0-android")
    implementation("com.google.android.play:app-update-ktx:2.1.0")

//    implementation("com.google.android.play:integrity:1.3.0")

    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("com.google.android.play:review-ktx:2.0.1")
}