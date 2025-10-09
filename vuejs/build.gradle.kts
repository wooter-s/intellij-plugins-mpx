// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

apply(from = "../contrib-configuration/common.gradle.kts")

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "2.1.0"
  id("org.jetbrains.intellij.platform") version "2.9.0"
}

repositories {
  intellijPlatform {
    defaultRepositories()
  }
}

intellijPlatform {
  pluginConfiguration {
    name = "Mpx.js"
  }
}

dependencies {
  intellijPlatform {
    bundledPlugins("JavaScript", "JSIntentionPowerPack", "JavaScriptDebugger", "com.intellij.css", "HtmlTools",
      "org.jetbrains.plugins.sass", "org.jetbrains.plugins.less", "intellij.webpack",
      "org.intellij.plugins.postcss", "intellij.prettierJS", "org.intellij.plugins.markdown")
    jetbrainsRuntime()
    intellijIdeaUltimate(ext("platform.version")) {
      useInstaller = false
    }
    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.Plugin.JavaScript)
  }
  implementation("org.apache.commons:commons-text:1.10.0")
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.fromTarget(ext("kotlin.jvmTarget")))
    @Suppress("UNCHECKED_CAST")
    freeCompilerArgs.addAll(rootProject.extensions["kotlin.freeCompilerArgs"] as List<String>)
    freeCompilerArgs.add("-Xwhen-guards")
  }
}

tasks {
  java {
    sourceCompatibility = JavaVersion.toVersion(ext("java.sourceCompatibility"))
    targetCompatibility = JavaVersion.toVersion(ext("java.targetCompatibility"))
  }
  wrapper {
    gradleVersion = ext("gradle.version")
  }
}

sourceSets {
  main {
    java {
      setSrcDirs(listOf("src", "gen"))
    }
    resources {
      setSrcDirs(listOf("resources"))
    }
  }
  test {
    java {
      //setSrcDirs(listOf("vuejs-tests/src"))
    }
  }
}


fun ext(name: String): String =
  rootProject.extensions[name] as? String
    ?: error("Property `$name` is not defined")