@file:OptIn(ApolloExperimental::class)

import com.apollographql.apollo.annotations.ApolloExperimental
import groovy.json.JsonSlurper
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget




plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.apolloGraphQl)
    id("org.jetbrains.kotlin.native.cocoapods")
}

// Push Android (FCM) sin el plugin com.google.gms.google-services, igual que LlegoApk:
// composeApp/google-services.json se lee aquí y sus valores se exponen como BuildConfig para
// inicializar Firebase a mano en LlegoBusinessApplication. Sin el archivo la build sigue
// funcionando y el push queda inactivo.
val androidApplicationId = "com.llego.business"

@Suppress("UNCHECKED_CAST")
fun readFirebaseConfig(): Map<String, String> {
    val googleServicesFile = file("google-services.json")
    if (!googleServicesFile.exists()) return emptyMap()

    val json = JsonSlurper().parse(googleServicesFile) as Map<String, Any?>
    val projectInfo = json["project_info"] as? Map<String, Any?> ?: emptyMap()
    val clients = json["client"] as? List<Map<String, Any?>> ?: emptyList()
    val client = clients.firstOrNull {
        val clientInfo = it["client_info"] as? Map<String, Any?>
        val androidInfo = clientInfo?.get("android_client_info") as? Map<String, Any?>
        androidInfo?.get("package_name") == androidApplicationId
    } ?: throw GradleException(
        "composeApp/google-services.json no contiene la app Android \"$androidApplicationId\". " +
            "Descárgalo de Firebase (proyecto llego-cd1c0) con esa app agregada."
    )
    val clientInfo = client["client_info"] as? Map<String, Any?> ?: emptyMap()
    val apiKeys = client["api_key"] as? List<Map<String, Any?>> ?: emptyList()

    return mapOf(
        "FIREBASE_PROJECT_ID" to (projectInfo["project_id"] as? String ?: ""),
        "FIREBASE_SENDER_ID" to (projectInfo["project_number"] as? String ?: ""),
        "FIREBASE_APP_ID" to (clientInfo["mobilesdk_app_id"] as? String ?: ""),
        "FIREBASE_API_KEY" to (apiKeys.firstOrNull()?.get("current_key") as? String ?: ""),
    )
}

val firebaseConfig = readFirebaseConfig()

val enableDesktop = providers.gradleProperty("llego.enableDesktop")
    .map { value -> value.equals("true", ignoreCase = true) }
    .orElse(false)
    .get()

val enableDesktopDev = providers.gradleProperty("llego.desktopDev")
    .map { value -> value.equals("true", ignoreCase = true) }
    .orElse(false)
    .get()

if (enableDesktop && enableDesktopDev) {
    apply(plugin = "org.jetbrains.compose.hot-reload")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    iosArm64()
    iosSimulatorArm64()
    
    cocoapods {
        summary = "Compose Logic"
        homepage = "https://github.com/JetBrains/compose-multiplatform"
        version = "1.0"
        // Igual que iosApp (IPHONEOS_DEPLOYMENT_TARGET) y el platform de iosApp/Podfile
        ios.deploymentTarget = "16.0"
        podfile = project.file("../iosApp/Podfile")
        
        framework {
            baseName = "ComposeApp"
            isStatic = true
        }

        pod("GoogleSignIn") {
            version = "7.1.0"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
    }

    if (enableDesktop) {
        jvm()
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.google.maps.compose)
            implementation(libs.play.services.maps)
            implementation(libs.ktor.client.android)
            // Google Sign-In for Android
            implementation(libs.play.services.auth)
            // Encrypted SharedPreferences for secure token storage
            implementation(libs.androidx.security.crypto)
            // Custom Tabs for Apple Sign-In OAuth flow
            implementation(libs.androidx.browser)
            // Push (Firebase Cloud Messaging)
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.messaging)
        }
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
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.apollo.runtime)
            implementation(libs.apollo.normalized.cache)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            // Coil for image loading
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        if (enableDesktop) {
            jvmMain.dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutinesSwing)
                implementation(libs.ktor.client.okhttp)
            }
        }
    }
}

android {
    namespace = "com.llego.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = androidApplicationId
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        listOf("FIREBASE_PROJECT_ID", "FIREBASE_SENDER_ID", "FIREBASE_APP_ID", "FIREBASE_API_KEY").forEach { key ->
            buildConfigField("String", key, "\"${firebaseConfig[key] ?: ""}\"")
        }
    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

if (enableDesktop) {
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
}

apollo {
    service("service") {
        packageName.set("com.llego.multiplatform.graphql")

        // Tells Apollo to generate Kotlin models
        generateKotlinModels.set(true)

        // Reduce Apollo generated code when fake data builders are not used.
        generateDataBuilders.set(false)

        // Introspection configuration to download schema via Gradle
        introspection {
            endpointUrl.set("https://llegobackend-production.up.railway.app/graphql")
            schemaFile.set(file("src/commonMain/graphql/schema.graphqls"))
        }
    }
}

// El Podfile sintético que genera KGP para compilar pods desde Gradle solo sube el
// deployment target de los pods a 11.0 (KT-57741). Xcode 26+ exige >= 15, así que
// GoogleSignIn/AppAuth/GTM* fallan con "deployment target 11.0". Lo subimos a 16.0,
// igual que hace el post_install de iosApp/Podfile para las builds desde Xcode.
tasks.matching { it.name == "podGenIos" }.configureEach {
    // Locales (no top-level): la configuration cache no serializa referencias al script.
    val syntheticPodsDeploymentTarget = "16"
    val syntheticIosPodfile = project.layout.buildDirectory.file("cocoapods/synthetic/ios/Podfile")
    doLast {
        val podfile = syntheticIosPodfile.get().asFile
        if (!podfile.exists()) return@doLast
        val original = podfile.readText()
        val patched = original
            .replace("deployment_target_major < 11 ||", "deployment_target_major < $syntheticPodsDeploymentTarget ||")
            .replace("deployment_target_major == 11 &&", "deployment_target_major == $syntheticPodsDeploymentTarget &&")
            .replace("\"#{11}.#{0}\"", "\"#{$syntheticPodsDeploymentTarget}.#{0}\"")
        if (patched == original) {
            logger.warn("w: No se pudo subir el deployment target de los pods sintéticos: cambió el formato del Podfile generado por KGP")
        } else {
            podfile.writeText(patched)
        }
    }
}
