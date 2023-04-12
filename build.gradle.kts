import org.jetbrains.changelog.markdownToHTML

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    id("org.jetbrains.intellij") version "1.12.0"
    // https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "1.3.1"
}


group = "com.xdd"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2021.3.2")
    plugins.set(
        listOf("yaml", "java", "Dart:213.7433", "io.flutter:70.2.3", "Kotlin")
    )
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}

version = 1.0
tasks.patchPluginXml {
    sinceBuild.set("200")
    untilBuild.set("")
    pluginId.set("com.xdd.flutter_code_helper")
    pluginDescription.set(markdownToHTML(File(rootDir, "doc.md").readText()))
    changeNotes.set(
        """
            <h1>1.0</h1>
            <ul>
                <li>which can automatically generate defined folders and their subfolder</li>
                <li>Preliminary completion function, </li>
            </ul>
        """.trimIndent()
    )
}
