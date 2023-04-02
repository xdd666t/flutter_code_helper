plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    id("org.jetbrains.intellij") version "1.12.0"
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

tasks.patchPluginXml {
    sinceBuild.set("200.*")
    untilBuild.set("*.*")
    pluginId.set("com.xdd.flutter_code_helper")
    version.set("1.0")

    pluginDescription.set(
        """
            <h2>Flutter Code Helper</h2>
            <ul>
                <h3>Introduction</h3>
                <li>usage: You need to add the following to pubspec.yaml</a></li>
                <br>code_helper:<br>
                  # fill in the folders you need to automatically generate<br>
                  auto_folder: ["assets/", "lib/widgets/"]
            </ul>
        """.trimIndent()
    )

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
