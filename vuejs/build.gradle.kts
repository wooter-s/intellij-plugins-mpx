// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask

apply(from = "../contrib-configuration/common.gradle.kts")

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.intellij.platform")
}

val localIdePath = providers.gradleProperty("mpx.localIdePath").orNull

repositories {
  intellijPlatform {
    defaultRepositories()
    snapshots()
  }
}

intellijPlatform {
  pluginConfiguration {
    name = "Mpx.js"
    version = providers.gradleProperty("mpx.pluginVersion")
    ideaVersion {
      sinceBuild = "262.8665"
      untilBuild = "262.*"
    }
  }
  pluginVerification {
    // This project is a source-level adaptation of JetBrains' bundled Vue plugin and therefore
    // intentionally uses the same internal/experimental APIs. Keep binary, dependency, and archive
    // integrity failures blocking while still reporting the inherited API-status warnings.
    failureLevel = listOf(
      VerifyPluginTask.FailureLevel.COMPATIBILITY_PROBLEMS,
      VerifyPluginTask.FailureLevel.PLUGIN_STRUCTURE_WARNINGS,
      VerifyPluginTask.FailureLevel.MISSING_DEPENDENCIES,
      VerifyPluginTask.FailureLevel.INVALID_PLUGIN,
    )
    ides {
      if (localIdePath != null) {
        local(localIdePath)
      }
      else {
        create(IntelliJPlatformType.WebStorm, providers.gradleProperty("mpx.platformVersion"))
      }
    }
  }
}

dependencies {
  intellijPlatform {
    jetbrainsRuntime()
    if (localIdePath != null) {
      local(localIdePath)
    }
    else {
      webstorm(providers.gradleProperty("mpx.platformVersion"))
    }

    bundledPlugins(
      "JavaScript",
      "org.intellij.plugins.postcss",
      "com.intellij.css",
      "org.jetbrains.plugins.sass",
      "org.jetbrains.plugins.less",
      "HtmlTools",
      "com.intellij.copyright",
      "intellij.webpack",
      "JSIntentionPowerPack",
      "JavaScriptDebugger",
      "intellij.javascript.eslint",
    )

    pluginModule(implementation(project(":vuejs-common")))
    pluginModule(implementation(project(":vuejs-backend")))
    pluginModule(implementation(project(":vuejs-webpack")))
    pluginModule(implementation(project(":vuejs-eslint")))
    pluginModule(implementation(project(":vuejs-debugger")))
    pluginModule(implementation(project(":vuejs-markdown")))
    pluginModule(implementation(project(":vuejs-prettier")))
    pluginModule(implementation(project(":vuejs-copyright")))

    // Marketplace-only plugins used by some tests (not bundled in IDEA Ultimate); set a version to enable:
    // plugin("org.jetbrains.plugins.stylus", "<version>")
    // plugin("com.jetbrains.plugins.Jade", "<version>")
  }

  testImplementation("junit:junit:${ext("junit.version")}")
}

kotlin {
  jvmToolchain(25)
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_25)
    @Suppress("UNCHECKED_CAST")
    freeCompilerArgs.addAll(rootProject.extensions["kotlin.freeCompilerArgs"] as List<String>)
  }
}

tasks {
  buildPlugin {
    archiveFileName = "mpxjs-${providers.gradleProperty("mpx.pluginVersion").get()}.zip"
  }

  wrapper {
    // Local override of the shared Gradle version: IntelliJ Platform Gradle Plugin 2.16 requires Gradle 9+.
    gradleVersion = "9.5.1"
  }
}

sourceSets {
  main {
    resources {
      setSrcDirs(listOf("vuejs-plugin/resources"))
    }
  }
  test {
    java {
      // The upstream test suites depend on IntelliJ monorepo-only test modules.
      // Keep standalone Gradle verification focused on the MPX-specific compatibility surface.
      setSrcDirs(listOf("vuejs-tests/standalone"))
    }
    resources {
      setSrcDirs(listOf("vuejs-tests/testData"))
    }
  }
}

fun ext(name: String): String =
  rootProject.extensions[name] as? String
  ?: error("Property `$name` is not defined")
