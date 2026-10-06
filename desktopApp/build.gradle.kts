import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(compose.components.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "com.rapiddev.ah.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
            packageName = "AH RapidDev"
            packageVersion = "1.0.0"
            description = "AH RapidDev - Desktop Edition"
            vendor = "AH"

            windows {
                iconFile.set(project.file("icon.ico"))
                menuGroup = "AH RapidDev"
                shortcut = true
                dirChooser = true
                perUserInstall = true
                upgradeUuid = "b7e8c8d0-1a2b-4c3d-9e5f-6a7b8c9d0e1f"
            }
        }
    }
}