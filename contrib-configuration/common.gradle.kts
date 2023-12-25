repositories {
  // 阿里云镜像
  maven {
    url = uri("https://maven.aliyun.com/repository/public")
  }
  mavenCentral()
  maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
  maven("https://www.jetbrains.com/intellij-repository/snapshots")
  maven("https://packages.jetbrains.team/maven/p/grazi/grazie-platform-public")
  maven("https://download.jetbrains.com/teamcity-repository")
}

// Check marketplace for published plugin versions
rootProject.extensions.add("targetVersion","233.11799.172")
// See https://www.jetbrains.com/intellij-repository/snapshots - com.jetbrains.intellij.idea
rootProject.extensions.add("iuVersion","233.13135.103")