plugins {
    alias(libs.plugins.android.application)
    // Dùng Kotlin 2.0.21 (Bản mới nhất, ổn định)
    id("org.jetbrains.kotlin.android") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

android {
    namespace = "com.example.appmangxahoi"
    compileSdk = 35 // BẮT BUỘC: Phải là 35 để chạy được các thư viện mới nhất

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    implementation("androidx.compose.material3:material3")

    // Icon mở rộng
    implementation("androidx.compose.material:material-icons-extended")

    // 3. THƯ VIỆN KHÁC
    // Load ảnh từ mạng
    implementation("io.coil-kt:coil-compose:2.7.0")

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
}