plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") // Using KSP instead of KAPT
}

android {
    namespace = "com.example.budgetpiggy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.budgetpiggy"
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
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.media3.common.ktx)
    val room_version = "2.7.0" // Use the most recent stable compatible version with KSP
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")
    implementation("androidx.room:room-runtime:$room_version")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi:1.14.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("org.bouncycastle:bcprov-jdk15to18:1.70")

    ksp("androidx.room:room-compiler:$room_version")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.androidx.junit.ktx) // Useful for AndroidX JUnit extensions
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(kotlin("test"))
}
