// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

// pluginManagement 必须在顶层,不能嵌套在 buildscript 内
pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

buildscript {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}