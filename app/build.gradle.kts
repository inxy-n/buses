
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.inxy.buses"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.inxy.buses"
        minSdk = 31
        targetSdk = 34
        versionCode = 8
        versionName = "1.8"

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
    implementation(libs.gson) // 检查最新版本
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.play.services.basement)
    implementation(libs.play.services.location)
    implementation(files("libs/AMap_Location_V6.4.7_20240816.jar"))

    testImplementation(libs.junit.junit)

    implementation("com.squareup.moshi:moshi:1.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.12.0")
        implementation ("androidx.core:core-ktx:1.6.0")
        implementation ("androidx.appcompat:appcompat:1.3.1")
        implementation ("com.google.android.material:material:1.4.0")
        implementation ("androidx.constraintlayout:constraintlayout:2.1.0")

    //implementation(libs.androidx.navigation.ui.ktx)
    //implementation(libs.androidx.room.compiler.processing.testing)
    //implementation(files("src/main/libs/AMap_Location_V6.4.7_20240816.jar"))
    //testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}