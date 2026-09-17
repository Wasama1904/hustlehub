dependencies {
    // Compose BOM + Material 3
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // MVVM
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    // Room offline-first
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Firebase BaaS - no custom backend needed
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // REST - ExchangeRate + ZenQuotes
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Security - encrypt password requirement
    implementation("androidx.security:security-crypto:1.1.0")

    // WorkManager for sync, DataStore for settings
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}