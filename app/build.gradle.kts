plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.asierla.das_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.asierla.das_app"
        minSdk = 29
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.play.services.maps)
    implementation("com.google.android.gms:play-services-location:18.0.0")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("androidx.drawerlayout:drawerlayout:1.1.1")
    implementation ("androidx.navigation:navigation-ui:2.3.4")
    implementation(libs.gridlayout)
    implementation(libs.recyclerview)
    implementation(libs.work.runtime)
    implementation("junit:junit:4.13.2") {
        exclude(group = "org.hamcrest", module = "hamcrest-core")
    }
    implementation(libs.firebase.messaging)
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("androidx.work:work-runtime:2.7.1")
    implementation ("com.googlecode.json-simple:json-simple:1.1.1")
    implementation ("com.android.volley:volley:1.2.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5") // Use the latest version
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("com.google.android.material:material:1.6.0") // Para que la foto de perfil salga en el circulo
}
