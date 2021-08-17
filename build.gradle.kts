import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
  java
  idea
  `maven-publish`
  kotlin("jvm") version Versions.kotlin
}

group = "gg.mixtape"
version = "3.0.0"

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

  compileOnly("org.reflections:reflections:0.9.12")
  api("net.dv8tion:JDA:4.3.0_309")
  api("org.slf4j:slf4j-api:1.7.25")
}

/* publishing */
java {
  withSourcesJar()
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      groupId = "com.github.mixtape"
      artifactId = "flight"
      version = version
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
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "15"
    incremental = true
    freeCompilerArgs = listOf(
      CompilerArgs.requiresOptIn,
      CompilerArgs.experimentalStdlibApi
    )
  }
}

tasks.withType<Wrapper> {
    gradleVersion = "7.0"
    distributionType = Wrapper.DistributionType.ALL
}
