import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    // alias(libs.plugins.composeHotReload) // COMENTADO TEMPORALMENTE
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    // Android Target
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // iOS ARM64 (dispositivos reales)
    iosArm64 {
        binaries.framework {
            baseName = "ComposeApp"
            isStatic = true

            // Agregar bundle ID
            binaryOption("bundleId", "com.llego.business")

            // IMPORTANTE: Reducir optimizaciones en Release
            if (buildType == org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.RELEASE) {
                freeCompilerArgs += listOf(
                    "-Xdisable-phases=SpecialBackendChecks",
                    "-opt-in=kotlin.ExperimentalStdlibApi"
                )
                optimized = false  // Desactiva optimizaciones agresivas
            }
        }
    }

// iOS Simulator ARM64
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOption("bundleId", "com.llego.business")

            if (buildType == org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.RELEASE) {
                freeCompilerArgs += listOf(
                    "-Xdisable-phases=SpecialBackendChecks",
                    "-opt-in=kotlin.ExperimentalStdlibApi"
                )
                optimized = false
            }
        }
    }

// iOS X64
    iosX64 {
        binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOption("bundleId", "com.llego.business")

            if (buildType == org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.RELEASE) {
                freeCompilerArgs += listOf(
                    "-Xdisable-phases=SpecialBackendChecks",
                    "-opt-in=kotlin.ExperimentalStdlibApi"
                )
                optimized = false
            }
        }
    }

    jvm()

    sourceSets {
        // Common Main
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        // Android Main
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.google.maps.compose)
            implementation(libs.play.services.maps)
        }

        // iOS Common - IMPORTANTE: Configuración explícita
        val iosMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                // Dependencias específicas de iOS si las necesitas
            }
        }

        // iOS ARM64
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }

        // iOS Simulator ARM64
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }

        // iOS X64
        val iosX64Main by getting {
            dependsOn(iosMain)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }

    // IMPORTANTE: Configuración global para suprimir warnings
    targets.configureEach {
        compilations.configureEach {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
}

android {
    namespace = "com.llego.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.llego.business"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.llego.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.llego.business"
            packageVersion = "1.0.0"
        }
    }
}