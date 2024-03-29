import java.io.FileInputStream
import java.util.Properties

val envProperties = Properties()
envProperties.load(
  FileInputStream(rootProject.file("env.properties"))
)

plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  kotlin("plugin.serialization") version "1.8.10"
  id("com.google.gms.google-services")
}

android {
  namespace = "nl.hva.chatstone"
  compileSdk = 34

  defaultConfig {
    applicationId = "nl.hva.chatstone"
    minSdk = 31
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
    
    buildFeatures {
      buildConfig = true
      buildConfigField("String", "FIREBASE_BUCKET", envProperties["FIREBASE_BUCKET"] as String)
      buildConfigField("String", "API_BASE_URL", envProperties["API_BASE_URL"] as String)
      buildConfigField("String", "METERED_USERNAME", envProperties["METERED_USERNAME"] as String)
      buildConfigField("String", "METERED_PASSWORD", envProperties["METERED_PASSWORD"] as String)
    }
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
  kotlinOptions {
    jvmTarget = "17"
  }
  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = "1.4.3"
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

dependencies {
  implementation("androidx.core:core-ktx:1.12.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
  implementation("androidx.activity:activity-compose:1.8.2")
  implementation(platform("androidx.compose:compose-bom:2023.10.01"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3")

  // debugger
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")

  // tests
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
  androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")

  // material icons
  implementation("androidx.compose.material:material-icons-extended")

  // live data
  implementation("androidx.compose.runtime:runtime-livedata:1.5.4")
  implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

  // navigation
  implementation("androidx.navigation:navigation-compose:2.7.6")
  implementation("androidx.compose.material:material:1.5.4")

  // splash screen
  implementation("androidx.core:core-splashscreen:1.0.1")

  // networking
  implementation("com.squareup.retrofit2:retrofit:2.9.0")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
  implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

  // datastore
  implementation("androidx.datastore:datastore-preferences:1.0.0")

  // firebase
  implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
  implementation("com.google.firebase:firebase-storage")

  // image loading
  implementation("io.coil-kt:coil:2.1.0")
  implementation("io.coil-kt:coil-compose:2.1.0")

  // webrtc
  implementation("io.getstream:stream-webrtc-android:1.1.1")
}