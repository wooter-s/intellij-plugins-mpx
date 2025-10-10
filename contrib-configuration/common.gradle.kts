repositories {
  mavenCentral()
  maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

rootProject.extensions.add("platform.version", "2025.2.2")

rootProject.extensions.add("gradle.version", "8.11.1")

rootProject.extensions.add("junit.version", "4.13.2")

rootProject.extensions.add("kotlin.jvmTarget", "17")
rootProject.extensions.add("kotlin.freeCompilerArgs", listOf("-Xjvm-default=all"))

rootProject.extensions.add("java.sourceCompatibility", "17")
rootProject.extensions.add("java.targetCompatibility", "17")