import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

/**
 * Firma de release. Los datos NO están en el repositorio:
 * - En local: fichero indicado por la variable CONEJO_KEYSTORE_PROPERTIES
 *   (por defecto ~/.config/conejojorge/keystore.properties).
 * - En GitHub Actions: variables de entorno a partir de los secretos del repo.
 * Si no hay datos de firma, el build de release se genera sin firmar.
 */
val signingProps = Properties().apply {
    val path = System.getenv("CONEJO_KEYSTORE_PROPERTIES")
        ?: "${System.getProperty("user.home")}/.config/conejojorge/keystore.properties"
    val file = File(path)
    if (file.isFile) file.inputStream().use { load(it) }
}

fun signingValue(key: String, env: String): String? = System.getenv(env) ?: signingProps.getProperty(key)

val releaseStoreFile = signingValue("storeFile", "CONEJO_STORE_FILE")

android {
    namespace = "com.example.conejojorge"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.jocude.conejojorge"
        minSdk = 29
        targetSdk = 36
        versionCode = 3
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = file(releaseStoreFile)
                storePassword = signingValue("storePassword", "CONEJO_STORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "CONEJO_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "CONEJO_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            // R8: reduce y ofusca el código y quita los recursos que no se usan
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
        }
        debug {
            // La versión de pruebas se instala junto a la de Play Store sin sustituirla
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
