import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.openapitools.codegen.CodegenConstants.ENUM_PROPERTY_NAMING
import org.openapitools.codegen.CodegenConstants.ENUM_PROPERTY_NAMING_TYPE
import org.openapitools.codegen.CodegenConstants.LIBRARY
import org.openapitools.codegen.languages.KotlinClientCodegen.DATE_LIBRARY
import org.openapitools.codegen.languages.KotlinClientCodegen.DateLibrary.KOTLINX_DATETIME
import org.openapitools.codegen.languages.KotlinClientCodegen.IDEA
import org.openapitools.codegen.languages.KotlinClientCodegen.OMIT_GRADLE_PLUGIN_VERSIONS
import org.openapitools.codegen.languages.KotlinClientCodegen.OMIT_GRADLE_WRAPPER
import org.openapitools.codegen.languages.KotlinClientCodegen.USE_COROUTINES
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.kotlin.serialization)
}

val openApiProperties =
    mapOf(
        USE_COROUTINES to true,
        ENUM_PROPERTY_NAMING to ENUM_PROPERTY_NAMING_TYPE.original.name,
        IDEA to true,
        OMIT_GRADLE_WRAPPER to true,
        OMIT_GRADLE_PLUGIN_VERSIONS to true,
        DATE_LIBRARY to KOTLINX_DATETIME.value,
        LIBRARY to "multiplatform",
    )

private val contentApiSchemaFile = "content-api-schema.json"

val contentApi =
    tasks.register<GenerateTask>("content-api") {
        generatorName.set("kotlin")
        inputSpec.set(
            "$projectDir/$contentApiSchemaFile",
        )
        outputDir.set("$buildDir/generated")
        packageName.set("com.rogers.seekr.api")
        apiPackage.set("com.rogers.seekr.api.content")
        modelPackage.set("com.rogers.seekr.api.content.model")
        additionalProperties.putAll(openApiProperties)
    }

private val liveApiSchemaFile = "live-api-schema.json"

val liveApi =
    tasks.register<GenerateTask>("live-api") {
        generatorName.set("kotlin")
        inputSpec.set(
            "$projectDir/$liveApiSchemaFile",
        )
        outputDir.set("$buildDir/generated")
        packageName.set("com.rogers.seekr.api")
        apiPackage.set("com.rogers.seekr.api.live")
        modelPackage.set("com.rogers.seekr.api.live.model")
        additionalProperties.putAll(openApiProperties)
    }

val codegenFix =
    tasks.register<Task>("codegen-fix") {
        doLast {
            val filePath =
                "$buildDir/generated/src/commonMain/kotlin/com/rogers/seekr/api/auth/HttpBasicAuth.kt"
            val file = File(filePath)

            val lines = file.readLines()

            // multiplatform codegen bug workaround, InternalAPI is in another package
            val modifiedLines = lines.filterNot {
                it.contains("import io.ktor.util.InternalAPI") ||
                        it.contains("@OptIn(InternalAPI::class)")
            }

            file.writeText(modifiedLines.joinToString("\n"))

            lines.forEach { println(it) }
        }
    }

kotlin {

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.core)

            api(libs.ktor.client.core)
            api(libs.ktor.client.serialization)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.serialization.kotlinx.json)

            api(libs.kotlinx.datetime)

            implementation(libs.coil.compose.core)
            implementation(libs.coil.compose)
            implementation(libs.coil.mp)
            implementation(libs.coil.network.ktor)
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            api(libs.ktor.client.android)
        }

        wasmJsMain.dependencies {
            api(libs.ktor.client.js)
        }

        iosMain.dependencies {
            api(libs.ktor.client.ios)
        }

        androidMain.dependencies {
            api(libs.ktor.client.android)
        }
    }

    sourceSets["commonMain"].kotlin.srcDirs("$buildDir/generated/src/commonMain/kotlin")
}

android {

    namespace = "com.rogers.seekr"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.rogers.seekr"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

