plugins {
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
   // Dòng này dùng cho Compose (nếu có trong TOML)
}

android {
    namespace = "com.example.appmangxahoi"
    compileSdk = 35 // BẮT BUỘC: Phải là 35 để chạy được các thư viện mới nhất
    buildFeatures {
        compose = true
    }
    defaultConfig {
        applicationId = "com.example.appmangxahoi"
        minSdk = 24
        targetSdk = 34 // Target 34 hoặc 35 đều được
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    compileOptions {
        // Đưa Java về phiên bản 11 (hoặc 17 nếu bạn muốn dùng bản mới hơn)
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        // Đưa Kotlin về cùng phiên bản 11 để khớp với Java
        jvmTarget = "11"
    }
}

dependencies {
    // 1. CÁC THƯ VIỆN CỐT LÕI (Dùng phiên bản cứng ổn định để tránh lỗi SDK 36)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1") // Đã bao gồm setContent

    // 2. CẤU HÌNH COMPOSE (Dùng BOM mới hơn 2024.06.00 để hợp với Kotlin 2.0)
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Material Design 3 (Thư viện giao diện chính)
    implementation("androidx.compose.material3:material3:1.3.0")

    // Icon mở rộng
    implementation("androidx.compose.material:material-icons-extended")

    // 3. THƯ VIỆN KHÁC
    // Load ảnh từ mạng
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation(libs.androidx.ui.text)

    // 4. TESTING & DEBUG
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("com.google.android.material:material:1.12.0")

    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Thư viện phát Video (ExoPlayer)
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

}