plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.ajohearresp"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.ajohearresp"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    val cameraX_version = "1.2.3"
    implementation("androidx.room:room-common:2.5.2")
    implementation("androidx.camera:camera-lifecycle:$cameraX_version")
    implementation("androidx.camera:camera-core:$cameraX_version")
    implementation("androidx.camera:camera-camera2:$cameraX_version")
    implementation("androidx.camera:camera-view:$cameraX_version")
    implementation("androidx.camera:camera-video:$cameraX_version")
    implementation("androidx.camera:camera-extensions:$cameraX_version")



    val room_version = "2.5.2"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    // optional - RxJava2 support for Room
    implementation("androidx.room:room-rxjava2:$room_version")
    // optional - RxJava3 support for Room
    implementation("androidx.room:room-rxjava3:$room_version")
    // optional - Test helpers
    testImplementation("androidx.room:room-testing:$room_version")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-maps:18.0.0")
    implementation("com.google.android.gms:play-services-location:20.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


}