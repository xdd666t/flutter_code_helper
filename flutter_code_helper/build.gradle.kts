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
//    type.set("IC") // Target IDE Platform

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
            <h2>flutter code helper</h2>
            <ul>
                <h3>使用说明</h3>
                <li>介绍....</a></li>
                <li>介绍：<a href="https://juejin.cn/post/6984593635681517582"> 使用说明</a></li>
            </ul>
        """.trimIndent()
    )

    changeNotes.set(
        """
            <h1>1.0</h1>
            <ul>
                <li>You can generate a large number of GetX template codes</li>
                <li>Improve development efficiency</li>
                <li>If you have any questions, please give feedback</li>
            </ul>
        """.trimIndent()
    )
}
