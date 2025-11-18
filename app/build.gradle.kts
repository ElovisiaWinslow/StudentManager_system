plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    // 关键修改：将 namespace 改为原项目包名
    namespace = "com.example.studentmanager_system"
    compileSdk = 36

    defaultConfig {
        // 关键修改：applicationId 与 namespace 保持一致（应用唯一标识）
        applicationId = "com.example.studentmanager_system"
        minSdk = 26
        targetSdk = 36
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
        // 新增：启用详细的过时API警告
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "11"
        // 新增：启用Kotlin的过时API警告
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xlint:deprecation",
            "-opt-in=kotlin.RequiresOptIn"
        )
    }

    buildFeatures {
        compose = true  // 保持 Compose 配置不变（若使用 Compose UI）
    }

    // 新增：启用详细的构建警告
    lint {
        warningsAsErrors = false
        abortOnError = false
        checkDependencies = true
        // 启用过时API检查
        disable.addAll(listOf("GradleDependency", "ObsoleteLintCustomCheck"))
        enable.addAll(listOf("Deprecation", "UnusedResources", "ObsoleteSdkInt"))
    }
}

// 新增：配置所有任务的编译选项
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf(
        "-Xlint:deprecation",
        "-Xlint:unchecked"
    ))
    options.isDeprecation = true
}

dependencies {
    // 保持现有的 Compose 依赖
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.gridlayout)

    // 修改这些传统 View 系统依赖的版本
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")  // 改为 1.9.0 版本
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // 其他依赖保持不变...
    implementation("org.apache.poi:poi:5.2.4")
    implementation("org.apache.poi:poi-ooxml:5.2.4")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.viewpager2:viewpager2:1.0.0")
}