import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.BuiltArtifactsLoader
import com.android.build.api.variant.FilterConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.secrets.gradle.plugin)
}

val appVersionCode = providers.gradleProperty("APP_VERSION_CODE")
val appVersionName = providers.gradleProperty("APP_VERSION_NAME")

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "me.miran.libreinfo"
    compileSdk = 36

    defaultConfig {
        applicationId = "me.miran.libreinfo"
        minSdk = 26
        targetSdk = 36
        versionCode = appVersionCode.get().toInt()
        versionName = appVersionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a")
            isUniversalApk = true
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (keystorePropertiesFile.exists())
                signingConfigs.getByName("release") else null
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// AGP 9 removed the legacy `applicationVariants`/`outputFileName` rename API. The
// supported approach is to listen to the APK artifact and copy each output to a
// cleanly named file. Copies land in build/outputs/apk-renamed/<variant>/.
androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        val copyTask = tasks.register<CopyRenamedApks>(
            "copyRenamedApksFor${variant.name.replaceFirstChar { it.uppercase() }}"
        ) {
            output.set(layout.buildDirectory.dir("outputs/apk-renamed/${variant.name}"))
            builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
        }
        variant.artifacts.use(copyTask).wiredWith { it.input }.toListenTo(SingleArtifact.APK)

        val versionJsonTask = tasks.register(
            "generateVersionJsonFor${variant.name.replaceFirstChar { it.uppercase() }}"
        ) {
            val code = appVersionCode
            val name = appVersionName
            val outFile = layout.buildDirectory
                .file("outputs/apk-renamed/${variant.name}/version.json")

            val abiCfg = android.splits.abi
            val abis = buildList {
                addAll(android.splits.abiFilters)
                if (abiCfg.isUniversalApk) add("universal")
                if (isEmpty()) add("universal")
            }

            doLast {
                val v = name.get()
                val json = com.google.gson.JsonObject().apply {
                    addProperty("versionCode", code.get().toInt())
                    addProperty("versionName", v)

                    val apksList = com.google.gson.JsonObject().apply {
                        abis.forEach{ abi ->
                            val fileName = "LibreInfo-v$v-$abi.apk"
                            addProperty(abi, fileName)
                        }
                    }

                    add("apks", apksList)
                }
                outFile.get().asFile.apply {
                    parentFile.mkdirs()
                    writeText(json.toString())
                }
                println("Wrote ${outFile.get().asFile}")
            }
        }

        copyTask.configure { finalizedBy(versionJsonTask) }
    }
}

secrets {
    // This checked-in file provides fallback values so builds (e.g. CI) don't
    // fail when a secret is missing locally.
    defaultPropertiesFileName = "secrets.defaults.properties"
}

dependencies {

    implementation(platform(libs.compose.bom))
    androidTestImplementation(platform(libs.compose.bom))

    implementation(libs.material3)
    implementation(libs.activity.compose)
    implementation(libs.material.icons.extended)
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.work.runtime)

    implementation(libs.ui.tooling.preview)
    debugImplementation(libs.ui.tooling)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.flexbox)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.maplibre.sdk)
    implementation(libs.maplibre.annotation)
    implementation(libs.xz)
    implementation(libs.compose.shimmer)
    implementation(libs.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

/**
 * Copies each built APK to a cleanly named file - `LibreInfo-v<version>-<abi>.apk`.
 * The ABI is read from the APK's split metadata (the universal APK has no ABI filter).
 */
abstract class CopyRenamedApks : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val input: DirectoryProperty

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    @get:Internal
    abstract val builtArtifactsLoader: Property<BuiltArtifactsLoader>

    @TaskAction
    fun taskAction() {
        val outDir = output.get()

        val builtArtifacts = builtArtifactsLoader.get().load(input.get())
            ?: throw RuntimeException("Cannot load APKs")

        builtArtifacts.elements.forEach { artifact ->
            val abi = artifact.filters
                .firstOrNull { it.filterType == FilterConfiguration.FilterType.ABI }
                ?.identifier ?: "universal"
            val version = artifact.versionName?.takeIf { it.isNotBlank() }
                ?: builtArtifacts.variantName
            val name = "LibreInfo-v$version-$abi.apk"
            File(artifact.outputFile).copyTo(outDir.file(name).asFile, overwrite = true)
        }
    }
}