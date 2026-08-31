plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.vanniktech.mavenPublish)
}

android {
    namespace = "io.github.zakayothuku.recompositionhighlighter"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(
        groupId = "io.github.zakayothuku",
        artifactId = "compose-recomposition-highlighter",
        version = (project.findProperty("VERSION_NAME") as String?) ?: "1.0.0"
    )

    pom {
        name.set("compose-recomposition-highlighter")
        description.set("On-Device Visual Recomposition Heatmap Overlay & Real-Time Performance Audit Drawer for Jetpack Compose.")
        url.set("https://github.com/zakayothuku/compose-recomposition-highlighter")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("zakayothuku")
                name.set("Zakayo Thuku")
                email.set("zakayothuku@gmail.com")
            }
        }

        scm {
            connection.set("scm:git:github.com/zakayothuku/compose-recomposition-highlighter.git")
            developerConnection.set("scm:git:ssh://github.com/zakayothuku/compose-recomposition-highlighter.git")
            url.set("https://github.com/zakayothuku/compose-recomposition-highlighter")
        }
    }
}

plugins.withId("signing") {
    configure<SigningExtension> {
        val hasKey = project.hasProperty("signingInMemoryKey") ||
            project.hasProperty("signing.keyId") ||
            project.hasProperty("signing.secretKeyRingFile") ||
            System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey") != null
        isRequired = hasKey
    }
}
