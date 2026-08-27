plugins {
  alias(libs.plugins.android.library)
}

android {
  namespace = "is.xyz.mpv"
  compileSdk = 37

  defaultConfig {
    minSdk = 26
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    create("preview") {
      initWith(getByName("release"))
    }
  }

  buildFeatures {
    buildConfig = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  packaging {
    jniLibs {
      useLegacyPackaging = true
      pickFirsts += "**/libc++_shared.so"
    }
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
      excludes += "META-INF/DEPENDENCIES"
      excludes += "META-INF/LICENSE*"
      excludes += "META-INF/NOTICE*"
      excludes += "META-INF/*.kotlin_module"
    }
  }
}

dependencies {
  implementation("androidx.appcompat:appcompat:1.7.1")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}
