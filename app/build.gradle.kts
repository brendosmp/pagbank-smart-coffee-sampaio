plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "br.com.uol.pagbank.plugpagservice.demo"
    compileSdk = 34

    defaultConfig {
        applicationId = "br.com.uol.pagbank.plugpagservice.demo"
        minSdk = 23
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = 1
        versionName = "1.29.0"

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

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

//noinspection UseTomlInstead
dependencies {
    implementation(libs.plugpagwrapper)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.koin.core)
    implementation(libs.koin.android)

    implementation("org.nanohttpd:nanohttpd:2.3.1")
    implementation("com.google.code.gson:gson:2.11.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
