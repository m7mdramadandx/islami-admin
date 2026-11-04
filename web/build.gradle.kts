import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    js {
        outputModuleName = "islami-admin"
        browser {
            commonWebpackConfig {
                outputFileName = "islami-admin.js"
                // Pin the port: Firebase Auth persists the session per origin, so if the dev
                // server drifted to another port each run the admin would appear logged out.
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).copy(port = 8080)
            }
        }
        binaries.executable()
    }

    sourceSets {
        jsMain.dependencies {
            implementation(project(":shared"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.koin.core)
        }
    }
}
