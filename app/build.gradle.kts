import java.net.URI

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mhdstuff"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mhdstuff"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
//    implementation(libs.flexbox)
    // https://mvnrepository.com/artifact/com.github.google/flexbox-layout
//    implementation(libs.flexbox.layout)
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(libs.gson)
    implementation("org.maplibre.gl:android-sdk:11.0.0")
//    implementation("com.mapbox.mapboxsdk:mapbox-android-plugin-annotation-v9:0.9.0")
    implementation("org.maplibre.gl:android-plugin-annotation-v9:3.0.0")
    implementation("com.google.code.ksoap2-android:ksoap2-android:3.6.4")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}