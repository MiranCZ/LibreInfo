import java.net.URI

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "me.miran.mhdstuff"
    compileSdk = 35

    defaultConfig {
        applicationId = "me.miran.mhdstuff"
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
            signingConfig = signingConfigs.getByName("debug")
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
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(libs.gson)
    implementation("org.maplibre.gl:android-sdk:12.0.0")
    implementation("org.maplibre.gl:android-plugin-annotation-v9:3.0.2")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}