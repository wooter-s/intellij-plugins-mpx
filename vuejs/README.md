# Mpx.js for WebStorm

This plugin adds IDE support for [Mpx.js](https://mpxjs.cn/) single-file components (`.mpx`) to WebStorm.
It is adapted from JetBrains' Vue.js plugin and keeps the upstream parser, code insight, TypeScript service,
formatting, refactoring, and language-server integrations while adding Mpx and WeChat Mini Program semantics.

## Compatibility

- WebStorm 2026.2 (`262.*`), validated with build `WS-262.8665.341`
- JetBrains Runtime / JDK 25
- Gradle 9.5.1 (the checked-in wrapper)

The plugin ID is `org.wooter.mpxjs`; its module, package, registry, and language IDs are isolated from the bundled
Vue.js plugin so both plugins can be installed at the same time.

## Build and test

From this directory, use the installed WebStorm when it is available:

```shell
JAVA_HOME=/Applications/WebStorm.app/Contents/jbr/Contents/Home \
  ./gradlew test buildPlugin \
  -Pmpx.localIdePath=/Applications/WebStorm.app
```

Without `mpx.localIdePath`, Gradle resolves the WebStorm version declared in `gradle.properties`.
The distributable ZIP is written to `build/distributions/`.

The standalone Gradle test source set covers MPX-specific behavior and packaged-resource identity. The complete
upstream JetBrains test suites remain under `vuejs-tests/src` and `vuejs-tests/src-ext`; they require test modules
from the full IntelliJ monorepo and are therefore not part of the standalone Gradle run.

## Bundled language tools

Run the compatibility patch after regenerating or upgrading the bundled Vue language-server artifacts:

```shell
./vuejs-backend/scripts/patch-mpx-language-tools.sh
```

The patch switches the bundled language server, TypeScript plugin, and WebStorm TypeScript bridge from `.vue` to
`.mpx`. The bridge's npm workspace depends on the IntelliJ monorepo's JavaScript backend sources, so its generated
bundle is kept in this repository for standalone builds.
