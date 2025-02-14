import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
    `maven-publish`
    kotlin("jvm") version Versions.kotlin
}

group = "gg.mixtape"
version = "3.0.3"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    implementation(Dependencies.kotlinStdlib)
    implementation(Dependencies.kotlinReflect)
    implementation(Dependencies.kotlinxCoroutines)
    implementation(Dependencies.kotlinxCoroutinesJdk8)
    implementation("org.reflections:reflections:0.9.12")

    api("com.github.mixtape-oss:JDA:development-SNAPSHOT")
    api("org.slf4j:slf4j-api:1.7.32")

    testImplementation("ch.qos.logback:logback-classic:1.2.5")
}

/* publishing */
val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

publishing {
    repositories {
        maven {
            name = "jfrog"
            url = uri("https://dimensional.jfrog.io/artifactory/maven")
            credentials {
                username = System.getenv("JFROG_USERNAME")
                password = System.getenv("JFROG_PASSWORD")
            }
        }
    }

    publications {
        create<MavenPublication>("jfrog") {
            from(components["java"])

            group = project.group as String
            version = project.version as String
            artifactId = "flight"

            artifact(sourcesJar)
        }
    }
}

/* tasks */
fun getBuildVersion(): String {
    val gitVersion = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = gitVersion
    }

    return "$version\n${gitVersion.toString().trim()}"
}

tasks.create("writeVersion") {
    val resourcePath = sourceSets["main"].resources.srcDirs.first()
    val resources = file(resourcePath)
    if (!resources.exists()) {
        resources.mkdirs()
    }

    file("$resourcePath/flight.txt").writeText(getBuildVersion())
}

tasks.build {
    dependsOn("writeVersion")
    dependsOn(sourcesJar)
}

tasks.withType<KotlinCompile> {
    sourceCompatibility = "16"
    targetCompatibility = "16"
    kotlinOptions {
        jvmTarget = "16"
        incremental = true
        freeCompilerArgs = listOf(
            CompilerArgs.requiresOptIn,
            CompilerArgs.experimentalStdlibApi
        )
    }
}
