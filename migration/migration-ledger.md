# MPX WebStorm 262 原生标签能力账本

## Migration Charter

- 源能力：`253.29346.mpx-fresh` 中的 MPX/WXML 特殊原生标签元数据。
- 目标平台：WebStorm 2026.2.1，分支 `feat/mpx-ws262-native`。
- 范围：补齐 7 个特殊原生标签及 WXML `template` 属性，不迁移旧提交或旧 JSON。
- 验收：生成器测试、IDE 高亮测试、插件构建和 Plugin Verifier 全部通过。

| 批次 | 能力范围 | 状态 | 退出门禁 | 证据 |
| --- | --- | --- | --- | --- |
| M0 | `block`、`import`、`include`、`wxs`、`qs`、`page-meta`、`navigation-bar`、WXML `template` | done | 能力矩阵全部 `pass`，完整构建与兼容性验证通过 | 生成器 6/6、Gradle 61/61；`check`、结构校验与 Plugin Verifier 通过；实际 miniapp 沙箱加载 `.02` 成功 |

## 发布证据

- 版本：`262.9437.145.02`。
- 产物：`vuejs/build/distributions/mpxjs-262.9437.145.02.zip`。
- SHA-256：`06c0d424c34506b62d45ee1308bd6032bf389a473cc8d104af9b35c623dc0622`。
- Plugin Verifier：针对 `WS-262.9437.145` 的结论为 `Compatible`。
- 冒烟工程：`sass-miniapp/apps/miniapp`，确认 `.02` 插件、MPX 文件索引与 MPX Stub 索引正常加载，未出现 MPX 类加载或链接异常。
