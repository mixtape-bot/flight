import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version Versions.kotlin
}

group = "gg.mixtape"
version = "2.1.2"

repositories {
  jcenter()
  mavenCentral()
  maven("https://m2.dv8tion.net/releases")
}

dependencies {
  implementation(Dependencies.kotlinStdlib)
  implementation(Dependencies.kotlinReflect)
  implementation(Dependencies.kotlinxCoroutines)

  compileOnly("org.reflections:reflections:0.9.11")
  api("net.dv8tion:JDA:4.2.1_259")
  api("org.slf4j:slf4j-api:1.7.25")
}

//fun getBuildVersion(): String {
//    val gitVersion = ByteArrayOutputStream()
//    exec {
//      commandLine("git", "rev-parse", "--short", "HEAD")
//      standardOutput = gitVersion
//    }
//
//    return "$version\n${gitVersion.toString().trim()}"
//}

//task writeVersion() {
//  def resourcePath = sourceSets.main.resources.srcDirs[0]
//  def resources = file(resourcePath)
//
//  if (!resources.exists()) {
//    resources.mkdirs()
//  }
//
//  file("$resourcePath/flight.txt").text = getBuildVersion()
//}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "1.8"
  }
}

tasks.withType<Wrapper> {
    gradleVersion = "6.8"
    distributionType = Wrapper.DistributionType.ALL
}
