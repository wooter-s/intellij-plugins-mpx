# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the **Mpx.js IntelliJ Plugin** - an IntelliJ IDEA plugin providing IDE support for [Mpx.js](https://mpxjs.cn) mini-program framework. It's a customized fork of the JetBrains Vue.js plugin.

| Property | Value |
|----------|-------|
| Plugin ID | `org.wooter.mpxjs` |
| Plugin Name | Mpx.js |
| File Extension | `.mpx` |
| Package | `org.jetbrains.mpxjs` |
| Language ID | `Mpx` |
| Framework ID | `mpx` |

## Build Commands

```bash
# Full build (compile + test + package)
./gradlew build

# Build plugin ZIP for deployment
./gradlew buildPlugin
# Output: build/distributions/mpx.zip

# Compile only (fastest, skip tests)
./gradlew classes

# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "org.jetbrains.mpxjs.lang.VueResolveTest"

# Run IDE with plugin for manual testing
./gradlew runIde

# Clean build
./gradlew clean build

# Verbose build with stack trace
./gradlew buildPlugin --debug --stacktrace
```

**Note**: Requires JDK 21. Set `JAVA_HOME` if needed:
```bash
JAVA_HOME=/path/to/jdk21 ./gradlew buildPlugin
```

## Project Structure

```
vuejs/
├── vuejs-backend/           # Core plugin logic (main module)
│   ├── src/org/jetbrains/mpxjs/
│   │   ├── lang/            # Language implementations (lexer, parser, PSI)
│   │   │   ├── expr/        # Mpx expression language (JS/TS in templates)
│   │   │   ├── html/        # Mpx HTML language
│   │   │   └── typescript/  # TypeScript/Volar LSP integration
│   │   ├── codeInsight/     # Code completion, navigation, documentation
│   │   ├── model/           # Component data model
│   │   ├── web/             # PolySymbols/Web symbols integration
│   │   ├── context/         # Framework context detection
│   │   ├── index/           # Stub indexes for fast component lookup
│   │   └── libraries/       # Framework support (Vuex, Nuxt, Pinia)
│   ├── resources/
│   │   ├── intellij.mpxjs.backend.xml  # Extension point registrations
│   │   └── web-types/       # Web Types definitions (wx: directives, etc.)
│   └── gen-resources/
│       └── vue-service/     # TypeScript plugin (ws-typescript-vue-plugin)
│
├── vuejs-common/            # Shared utilities and icons
├── vuejs-plugin/            # Plugin descriptor
│   └── resources/META-INF/plugin.xml
├── vuejs-tests/             # Test classes and fixtures
│   ├── src/                 # Test classes (uses vuejs namespace)
│   └── testData/            # Test fixtures and mock node_modules
├── vuejs-copyright/         # Copyright support
├── vuejs-debugger/          # Debugger integration
├── vuejs-markdown/          # Markdown integration
└── vuejs-prettier/          # Prettier integration
```

## Architecture

### Core Components

1. **Language Layer** (`vuejs-backend/src/.../lang/`)
   - `VueFileType` → File type registration for `.mpx`
   - `VueParserDefinition` → PSI tree construction
   - Expression languages: `VueJSLanguage`, `VueTSLanguage` for template expressions

2. **Code Intelligence** (`vuejs-backend/src/.../codeInsight/`)
   - `VueCompletionContributor`: Main code completion provider
   - `VueReferenceContributor`: Symbol resolution and navigation
   - Template scope providers for `wx:for`, `wx:if`, etc.

3. **Model Layer** (`vuejs-backend/src/.../model/`)
   - `VueModelManager`: Central component registry
   - `VueComponent`, `VueDirective`: Domain models

4. **Web Symbols** (`vuejs-backend/src/.../web/`)
   - `VueFramework`: Framework registration (ID = "mpx")
   - `VueSymbolQueryConfigurator`: Symbol query configuration
   - Web-types JSON files define directives and components

5. **TypeScript Integration** (`vuejs-backend/gen-resources/vue-service/`)
   - `ws-typescript-vue-plugin/`: Custom TypeScript plugin for `.mpx` files
   - Handles script extraction and type checking

### Key Extension Points

Defined in `intellij.mpxjs.backend.xml`:
- `com.intellij.mpxjs.templateScopesProvider` - Custom template scope providers
- `com.intellij.mpxjs.containerInfoProvider` - Component info providers

### Framework Detection

The plugin activates when:
- Files with `.mpx` extension are present
- `@mpxjs/core` dependency in package.json
- Framework ID check: `context.framework == "mpx"`

## Key Files for Common Tasks

| Task | Files |
|------|-------|
| Add new directive | `web-types/vue@3.4.0.web-types.json` |
| Modify file type | `VueFileType.kt`, `VueIndex.kt` (VUE_FILE_EXTENSION) |
| Change framework ID | `VueFramework.kt` (ID constant) |
| Add completion | `VueCompletionContributor.kt` |
| Register extension | `intellij.mpxjs.backend.xml` |
| TypeScript handling | `gen-resources/vue-service/node_modules/ws-typescript-vue-plugin/` |

## ID Conventions (Avoid Conflicts with Vue.js Plugin)

This plugin uses `mpx` prefix to avoid conflicts with the built-in Vue.js plugin:

| Type | Pattern | Example |
|------|---------|---------|
| Framework ID | `mpx` | `VueFramework.ID = "mpx"` |
| Index IDs | `mpx.*` | `mpx.mixin.binding.index` |
| Stub Serializer | `MPX:*`, `MPX-JS:*` | `MPX:INTERPOLATION_EXPR` |
| Inspection shortName | `Mpx*` | `MpxDataFunction` |
| Settings ID | `settings.mpx` | In projectConfigurable |
| Element Types | `MPX_*` | `MPX_EMBEDDED_CONTENT` |

## Testing

Tests are in `vuejs-tests/src/org/jetbrains/vuejs/` (note: still uses `vuejs` namespace for compatibility).

```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests "org.jetbrains.mpxjs.lang.VueHighlightingTest"
```

Test data fixtures are in `vuejs-tests/testData/`.

## Bundled Dependencies

Required IntelliJ plugins (from build.gradle.kts):
- `JavaScript` - JS/TS language support
- `com.intellij.css` - CSS support
- `HtmlTools` - HTML parsing
- `intellij.webpack` - Build tool integration
- `intellij.prettierJS` - Code formatting

---

## Mpx 特殊实现逻辑

本节详细说明 Mpx.js 插件相对于 Vue.js 插件的特殊实现，这是理解和维护插件的关键。

### wx: 指令实现

Mpx 使用 `wx:` 前缀的指令替代 Vue 的 `v-` 前缀指令。

**关键文件**：

1. **控制流分析** - `vuejs-backend/src/org/jetbrains/mpxjs/lang/expr/VueControlFlowBuilder.kt`
   ```kotlin
   // 第 127-131 行定义指令常量
   companion object {
     private const val V_IF = "wx:if"
     private const val V_ELSE_IF = "wx:elif"
     private const val V_ELSE = "wx:else"
   }
   ```
   处理方法：
   - `processWxIfDirective()` - 处理 wx:if
   - `processWxElifDirective()` - 处理 wx:elif
   - `processWxElseDirective()` - 处理 wx:else

2. **Web-types 定义** - `vuejs-backend/resources/web-types/vue@3.4.0.web-types.json`
   - 包含 652 个 wx: 指令定义
   - `"framework": "mpx"` - 指定框架
   - 支持的指令：wx:if, wx:elif, wx:else, wx:for, wx:model, wx:key, wx:show 等

### usingComponents 组件注册机制

与 Vue.js 的 JavaScript 组件注册不同，Mpx 使用 JSON 格式的 `usingComponents`：

```html
<script name='json'>
module.exports = {
  usingComponents: {
    'my-component': './components/my-component.mpx',
    'another-comp': '../shared/another-comp.mpx'
  }
}
</script>
```

**关键实现文件**：

| 文件 | 职责 |
|------|------|
| `CustomElementDescriptorProvider.kt` | 解析组件定义，提供组件描述符 |
| `VueTagNameProvider.kt` | 提供组件名称自动补全 |
| `ComponentWxmlUtils.kt` | 解析组件路径引用 |

**CustomElementDescriptorProvider.kt** (第 74-152 行)：
- `findTopJsonScriptVueTap()` - 查找 `<script name="json">` 标签
- 支持 JSObjectLiteralExpression 和 JsonObject 格式
- 支持动态组件注册（if 条件判断中的组件）

**VueTagNameProvider.kt**：
- `addTagNameVariants()` - 提供组件名称补全
- `getUsingComponentNames()` - 从 usingComponents 提取组件名称列表

**VueJSReferenceContributor.kt**：
- `VueUsingComponentsPathReferenceProvider` - 为组件路径提供文件引用
- 支持 Ctrl+Click 跳转到组件文件

### TypeScript 服务适配

位于 `vuejs-backend/gen-resources/vue-service/node_modules/ws-typescript-vue-plugin/`

**关键修改点**：

| 文件 | 修改内容 |
|------|----------|
| `index.js` | `.vue` → `.mpx` 文件扩展名检查 |
| `transformVueSfcFile.js` | 组件 shim 使用 `*.mpx` |
| `vueScriptCache.js` | 文件缓存键使用 `.mpx` |

**transformVueSfcFile.js** 第 9 行：
```javascript
const componentShim = "import componentDefinition from '*.mpx'; export default componentDefinition;";
```

### 框架标识符配置

| 位置 | 值 | 用途 |
|------|-----|------|
| `VueFramework.kt` 第74行 | `ID = "mpx"` | 框架唯一标识 |
| `VueIndex.kt` 第18行 | `VUE_FILE_EXTENSION = ".mpx"` | 文件扩展名 |
| `VueContext.kt` 第31行 | `WebFramework.get("mpx")` | 框架实例获取 |
| `intellij.mpxjs.backend.xml` | `id="mpx"` | 扩展点注册 |

---

## 与 Vue.js 插件的关键差异

| 特性 | Vue.js 插件 | Mpx.js 插件 |
|------|------------|------------|
| 文件扩展名 | `.vue` | `.mpx` |
| 框架 ID | `vue` | `mpx` |
| 条件渲染 | `v-if`, `v-else-if`, `v-else` | `wx:if`, `wx:elif`, `wx:else` |
| 列表渲染 | `v-for` | `wx:for` |
| 双向绑定 | `v-model` | `wx:model` |
| 组件注册 | `components: {}` (JS) | `usingComponents: {}` (JSON) |
| 组件定义位置 | `<script>` 中的 export | `<script name='json'>` |
| ID 前缀 | `VUE:`, `VUE-JS:` | `MPX:`, `MPX-JS:` |
| 索引 ID | `vue.*` | `mpx.*` |
| Inspection | `Vue*` | `Mpx*` |
| Settings ID | `settings.vue` | `settings.mpx` |

**ID 隔离说明**：为避免与内置 Vue.js 插件冲突，所有内部标识符都使用 `mpx` 或 `MPX` 前缀。

---

## 调试和开发指南

### 调试 wx: 指令补全

1. **检查 web-types 加载**
   - 文件：`vue@3.4.0.web-types.json`
   - 验证 `"framework": "mpx"` 配置
   - 检查 `enable-when` 条件是否满足

2. **验证框架 ID 匹配**
   ```kotlin
   // 确保检查的是 "mpx" 框架
   context.framework == VueFramework.ID  // VueFramework.ID = "mpx"
   ```

3. **断点位置**
   - `VueSymbolQueryConfigurator.getNameConversionRulesProviders()` - 框架检查入口
   - `VueAttributeNameCodeCompletionFilter.kt` - 属性补全过滤

### 调试组件自动补全

1. **检查 JSON script 识别**
   - 验证 `<script name='json'>` 被正确解析
   - 文件：`CustomElementDescriptorProvider.kt`

2. **验证 usingComponents 解析**
   - 断点：`CustomElementDescriptorProvider.provide()`
   - 检查 `findTopJsonScriptVueTap()` 返回值

3. **验证补全触发**
   - 断点：`VueTagNameProvider.addTagNameVariants()`
   - 检查 `getUsingComponentNames()` 返回的组件列表

### 调试组件路径跳转

1. **检查引用提供**
   - 文件：`VueJSReferenceContributor.kt`
   - 断点：`VueUsingComponentsPathReferenceProvider.getReferencesByElement()`

2. **验证路径解析**
   - 检查 `FileReferenceSet` 创建
   - 验证相对路径解析正确

### 常用调试命令

```bash
# 启动调试 IDE（支持断点）
./gradlew runIde

# 查看详细构建日志
./gradlew buildPlugin --info

# 清理并重新构建
./gradlew clean buildPlugin
```

### 日志输出位置

在代码中添加临时日志：
```kotlin
// 输出到 IDE 的 idea.log
com.intellij.openapi.diagnostic.Logger.getInstance(VueTagNameProvider::class.java)
    .info("Debug: component names = $componentNames")
```

查看日志：Help → Show Log in Finder/Explorer
