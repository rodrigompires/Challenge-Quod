plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "br.com.fiap.desafioquod"
    compileSdk = 35

    defaultConfig {
        applicationId = "br.com.fiap.desafioquod"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)



    //Compose animation
    implementation (libs.androidx.animation.vxyz)


    //Dependencias do RETROFIT
    implementation (libs.retrofit)
    implementation (libs.converter.gson)

    //Biblioteca de mais icones
    implementation (libs.androidx.material.icons.extended)

    // Biblioteca NavHost
    implementation (libs.androidx.navigation.compose)

    // Biblioteca coil
    implementation(libs.coil)
    //implementation(libs.coil.compose)

    // Biblioteca de animações
    implementation(libs.androidx.animation)

    // Biblioteca de GIFs animados
    implementation (libs.android.gif.drawable)
    implementation (libs.coil.compose.v140)
    implementation (libs.coil.gif)
    implementation (libs.coil.compose)
    implementation (libs.glide)

    //  Biblioteca de Navegação bottom bar
    implementation(libs.animated.navigation.bar)


    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.mlkit.vision)
    implementation(libs.androidx.camera.extensions)
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.permissions.v0332rc01)
    implementation(libs.accompanist.permissions.v0372)

    // ML Kit Face Detection
    // implementation("com.google.mlkit:face-detection:16.1.5")
    implementation(libs.face.detection)

    // Biometria - BiometricPrompt
    // implementation("androidx.biometric:biometric:1.4.0-alpha02")
    implementation(libs.androidx.biometric)

    // ML Kit Text Recognition v2
//    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.1")
    implementation(libs.gms.play.services.mlkit.text.recognition)

    // ML Kit Image Labeling
//    implementation("com.google.mlkit:image-labeling:17.0.9")
    implementation(libs.mlkit.image.labeling)

// ML Kit Document Scanner
    // implementation ("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0-beta1")
    implementation (libs.play.services.mlkit.document.scanner)

    // ML Kit Object Detection
//    implementation ("com.google.mlkit:object-detection:17.0.2")
    implementation ("com.google.mlkit:object-detection:17.0.2")


//    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(libs.androidx.appcompat)

//    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation(libs.androidx.fragment.ktx)

//    Play Services
//    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation (libs.play.services.location)


//    implementation ("androidx.exifinterface:exifinterface:1.4.0")
    implementation (libs.androidx.exifinterface)

//    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(libs.okhttp)

//    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation(libs.logging.interceptor)


}