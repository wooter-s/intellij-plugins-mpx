// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.PrepareSandboxTask
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask
import java.util.zip.ZipFile

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.intellij.platform")
}

apply(from = "../contrib-configuration/common.gradle.kts")

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
      sinceBuild = "262.9437"
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
      "intellij.libraries.misc.plugin",
      "intellij.testRunner.plugin",
      "intellij.structureView.plugin",
      "intellij.structuralSearch.plugin",
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
    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.Plugin.JavaScript)
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
  withType<PrepareSandboxTask>().configureEach {
    from("vuejs-backend/vue-language-tools") {
      into(pluginName.map { "$it/vue-language-tools" })
    }
    from("vuejs-backend/gen-resources/vue-service") {
      into(pluginName.map { "$it/vue-service" })
    }
  }

  buildPlugin {
    archiveFileName = "mpxjs-${providers.gradleProperty("mpx.pluginVersion").get()}.zip"
  }

  wrapper {
    // Local override of the shared Gradle version: IntelliJ Platform Gradle Plugin 2.16 requires Gradle 9+.
    gradleVersion = "9.5.1"
  }
}

val verifyMpxBundledServices by tasks.registering {
  group = "verification"
  description = "Verifies that the MPX plugin archive contains all bundled language services."
  dependsOn(tasks.buildPlugin)

  doLast {
    val archive = tasks.buildPlugin.get().archiveFile.get().asFile
    val requiredEntries = listOf(
      "mpxjs/vue-language-tools/language-server/2.2.10/bin/vue-language-server.js",
      "mpxjs/vue-language-tools/language-server/3.0.11/bin/vue-language-server.js",
      "mpxjs/vue-language-tools/language-server/3.3.5/bin/vue-language-server.js",
      "mpxjs/vue-language-tools/typescript-plugin/3.0.11/node_modules/@vue/typescript-plugin/dist/index.js",
      "mpxjs/vue-language-tools/typescript-plugin/3.3.5/node_modules/@vue/typescript-plugin/dist/index.js",
      "mpxjs/vue-service/node_modules/ws-typescript-vue-plugin/index.js",
    )
    val entries = ZipFile(archive).use { zip ->
      zip.entries().asSequence().map { it.name }.toSet()
    }
    val missing = requiredEntries.filterNot(entries::contains)
    check(missing.isEmpty()) {
      "Missing bundled language-service files in ${archive.name}: ${missing.joinToString()}"
    }
  }
}

tasks.check {
  dependsOn(verifyMpxBundledServices)
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
