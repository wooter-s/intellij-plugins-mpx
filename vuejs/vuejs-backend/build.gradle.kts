// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
plugins {
  id("vuejs-content-module")
  id("org.jetbrains.kotlin.plugin.serialization")
}

sourceSets {
  main {
    java {
      setSrcDirs(listOf("src", "gen"))
    }
    resources {
      setSrcDirs(listOf("resources", "gen-resources"))
    }
  }
}

dependencies {
  intellijPlatform {
    bundledPlugins("JavaScript", "com.intellij.css", "org.intellij.plugins.postcss", "com.intellij.modules.json", "intellij.webpack")
    bundledModules(
      "intellij.platform.backend",
      "intellij.spellchecker",
    )
  }

  implementation(project(":vuejs-common"))
}
