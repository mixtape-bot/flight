import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
    `maven-publish`
    kotlin("jvm") version Versions.kotlin
}

group = "gg.mixtape"
version = "2.4.2"

repositories {
    mavenCentral()
    maven("https://dimensional.jfrog.io/artifactory/maven")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

kotlin {
    explicitApi()
}

dependencies {
    implementation(Dependencies.kotlinStdlib)
    implementation(Dependencies.kotlinReflect)
    implementation(Dependencies.kotlinLogging)
    implementation(Dependencies.kotlinxCoroutines)
    implementation(Dependencies.kotlinxCoroutinesJdk8)

    implementation(Dependencies.arrow)
    implementation(Dependencies.reflections)

    api(Dependencies.jda)
    api(Dependencies.slf4j)

    testImplementation(Dependencies.logback)
}

/* publishing */
val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

publishing {
    repositories {
        maven("https://dimensional.jfrog.io/artifactory/maven") {
            name = "jfrog"
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
