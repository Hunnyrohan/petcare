plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.unique"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.unique"
        minSdk = 24
        targetSdk = 34
        versionCode = 2 // Incremented version code
        versionName = "1.1" // Updated version name

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    viewBinding {
        enable = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
            // Uncomment the lines below if you have a signing configuration
            /*
            signingConfig signingConfigs.release // Add this line if you have a signing config
            */
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
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)

    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.2.0")) // Use the latest BOM
    implementation("com.google.firebase:firebase-analytics-ktx") // Add ktx for analytics
    implementation("com.google.firebase:firebase-auth-ktx") // Use ktx for authentication
    implementation("com.google.firebase:firebase-firestore-ktx") // Use ktx for Firestore
    implementation("com.google.firebase:firebase-storage-ktx") // Use ktx for Storage

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation(libs.androidx.recyclerview)
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.8.8")

    // OkHttp for HTTP requests
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // Fuel for networking
    implementation("com.github.kittinunf.fuel:fuel:2.3.1") // Check for the latest version
    implementation("com.github.kittinunf.fuel:fuel-coroutines:2.3.1") // If using coroutines

    // Unit Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.0")

    implementation ("com.google.android.material:material:1.9.0")
    implementation ("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation ("com.google.android.gms:play-services-maps:18.0.2")
    implementation ("com.google.android.gms:play-services-location:18.0.0")
}
