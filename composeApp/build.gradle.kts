import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val appVersion = providers.gradleProperty("msa.app.version").get()
val androidVersionCode = providers.gradleProperty("msa.android.versionCode").get().toInt()

group = rootProject.group
version = appVersion

kotlin {
    jvmToolchain(17)

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.androidx.media3.exoplayer)
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.compose.ui.test)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            // Compose Desktop uses Swing/AWT. This artifact installs Dispatchers.Main
            // for lifecycle-runtime-compose and other Main-confined coroutines.
            implementation(libs.kotlinx.coroutines.swing)
        }

        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }

        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.test.runner)
            implementation(libs.androidx.test.ext.junit)
            implementation(libs.androidx.compose.ui.test.junit4)
            implementation(libs.androidx.compose.ui.test.accessibility)
        }
    }
}

android {
    namespace = "com.msa.compose_kmm"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.msa.bee"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = androidVersionCode
        versionName = appVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        val keystorePath = providers.environmentVariable("MSA_ANDROID_KEYSTORE_PATH").orNull
        val storePasswordValue = providers.environmentVariable("MSA_ANDROID_STORE_PASSWORD").orNull
        val keyAliasValue = providers.environmentVariable("MSA_ANDROID_KEY_ALIAS").orNull
        val keyPasswordValue = providers.environmentVariable("MSA_ANDROID_KEY_PASSWORD").orNull

        if (
            !keystorePath.isNullOrBlank() &&
            !storePasswordValue.isNullOrBlank() &&
            !keyAliasValue.isNullOrBlank() &&
            !keyPasswordValue.isNullOrBlank()
        ) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = storePasswordValue
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfigs.findByName("release")?.let { signingConfig = it }
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

    testOptions {
        animationsDisabled = true
    }
}

dependencies {
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

compose.resources {
    packageOfResClass = "compose_kmm.composeapp.generated.resources"
}

compose.desktop {
    application {
        mainClass = "com.msa.compose_kmm.MainKt"
        // Required by Skiko on recent JDKs. It removes the restricted-native-access
        // warning and keeps the application compatible with upcoming JDK releases.
        jvmArgs("--enable-native-access=ALL-UNNAMED")

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "MSABee"
            packageVersion = appVersion
            description = "MSA Bee - a Compose Multiplatform arcade game"
            copyright = "Copyright (c) 2026 MSA / ALISCHILLER. All rights reserved."
            vendor = "MSA / ALISCHILLER"
            licenseFile.set(rootProject.file("LICENSE.md"))
            modules("java.desktop", "java.prefs")

            macOS {
                iconFile.set(project.file("src/jvmMain/resources/icons/msa-bee.icns"))
                bundleID = "com.msa.bee"
                dockName = "MSA Bee"
                minimumSystemVersion = "13.0"
                appCategory = "public.app-category.games"
            }

            windows {
                iconFile.set(project.file("src/jvmMain/resources/icons/msa-bee.ico"))
                menuGroup = "MSA"
                dirChooser = true
                perUserInstall = true
                upgradeUuid = "5C7E6A2D-58B4-4C96-8C06-D44BCF9CE8A1"
            }

            linux {
                iconFile.set(project.file("src/jvmMain/resources/icons/msa-bee.png"))
                packageName = "msa-bee"
                appCategory = "games"
                rpmLicenseType = "Proprietary"
            }
        }
    }
}


tasks.register("verifyBuildIdentity") {
    group = "verification"
    description = "Verifies release version coordinates before packaging."
    doLast {
        check(appVersion.matches(Regex("\\d+\\.\\d+\\.\\d+"))) {
            "msa.app.version must use MAJOR.MINOR.PATCH; actual=$appVersion"
        }
        check(androidVersionCode > 0) { "msa.android.versionCode must be positive" }
        check(project.version.toString() == appVersion) {
            "composeApp project.version must match msa.app.version"
        }
    }
}
