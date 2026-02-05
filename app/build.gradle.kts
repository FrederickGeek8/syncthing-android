import org.gradle.configurationcache.extensions.capitalized

plugins {
    id("com.android.application")
}

dependencies {
    implementation("eu.chainfire:libsuperuser:1.1.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("com.google.code.gson:gson:2.13.2") {
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
    }
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("com.google.guava:guava:32.1.3-android")
    implementation("com.annimon:stream:1.2.2")
    implementation("com.android.volley:volley:1.2.1")
    implementation("commons-io:commons-io:2.11.0")

    implementation("com.journeyapps:zxing-android-embedded:4.3.0") {
        isTransitive = false
    }
    implementation("com.google.zxing:core:3.4.1")

    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("com.google.dagger:dagger:2.57.2")
    annotationProcessor("com.google.dagger:dagger-compiler:2.57.2")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("androidx.annotation:annotation:1.2.0")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom:2.3.10"))
}

android {
    val ndkVersionShared = rootProject.extra.get("ndkVersionShared")
    // Changes to these values need to be reflected in `../docker/Dockerfile`
    ndkVersion = "${ndkVersionShared}"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.nutomic.syncthingandroid"
        minSdk = 21
        targetSdk = 33
        versionCode = 4402
        versionName = "2.0.14"
        testApplicationId = "com.nutomic.syncthingandroid.test"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = System.getenv("SYNCTHING_RELEASE_STORE_FILE")?.let(::file)
            storePassword = System.getenv("SIGNING_PASSWORD")
            keyAlias = System.getenv("SYNCTHING_RELEASE_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_PASSWORD")
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            isJniDebuggable = true
            isRenderscriptDebuggable = true
            isMinifyEnabled = false
        }
        getByName("release") {
            signingConfig = signingConfigs.runCatching { getByName("release") }
                .getOrNull()
                .takeIf { it?.storeFile != null }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Otherwise libsyncthing.so doesn't appear where it should in installs
    // based on app bundles, and thus nothing works.
    packaging.jniLibs.useLegacyPackaging = true
    namespace = "com.nutomic.syncthingandroid"
}

/**
 * Some languages are not supported by Google Play, so we ignore them.
 */
tasks.register<Delete>("deleteUnsupportedPlayTranslations") {
    delete(
        "src/main/play/listings/de_DE/",
        "src/main/play/listings/el-EL/",
        "src/main/play/listings/en/",
        "src/main/play/listings/eo/",
        "src/main/play/listings/eu/",
        "src/main/play/listings/nb/",
        "src/main/play/listings/nl_BE/",
        "src/main/play/listings/nn/",
        "src/main/play/listings/ta/",
    )
}

project.afterEvaluate {
    android.buildTypes.forEach {
        tasks.named("merge${it.name.capitalized()}JniLibFolders") {
            dependsOn(":syncthing:buildNative")
        }
    }
}
