object Versions {
    const val kotlin = "1.6.0"
    const val kotlinLogging = "2.0.11"
    const val kotlinxCoroutines = "1.5.2"

    const val arrow = "1.0.2-SNAPSHOT"
    const val reflections = "0.10.2"
    const val jda = "4.4.2_DEV"
    const val slf4j = "1.7.32"
    const val logback = "1.2.7"
}

object Dependencies {
    const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    const val kotlinLogging = "io.github.microutils:kotlin-logging-jvm:${Versions.kotlinLogging}"
    const val kotlinxCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinxCoroutines}"
    const val kotlinxCoroutinesJdk8 = "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.kotlinxCoroutines}"

    const val arrow = "io.arrow-kt:arrow-core:${Versions.arrow}"
    const val reflections = "org.reflections:reflections:${Versions.reflections}"
    const val jda = "net.dv8tion:JDA:${Versions.jda}"
    const val slf4j = "org.slf4j:slf4j-api:${Versions.slf4j}"
    const val logback = "ch.qos.logback:logback-classic:${Versions.logback}"
}
