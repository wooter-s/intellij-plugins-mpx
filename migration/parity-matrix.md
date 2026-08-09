# MPX WebStorm 262 原生标签一致性矩阵

| Capability ID | 源行为 | 目标行为 | 优先级 | 判定 | 证据 |
| --- | --- | --- | --- | --- | --- |
| CAP-001 | `block` 作为逻辑渲染容器并支持 `wx:*` | 由 262 Web Types 提供原生标签元数据 | P0 | pass | `testBlockAndWxsMetadataAreRecognized`；实际工程包含 13 个 `block` 用例 |
| CAP-002 | `import`、`include` 支持必填 `src` | 由 262 Web Types 提供标签、属性和文档 | P1 | pass | 生成器属性断言与 `testSpecialWxmlElementsAndTemplateAttributesAreRecognized` |
| CAP-003 | `wxs` 支持 `module`、`src` | 由 262 Web Types 提供标签、属性和文档 | P0 | pass | `testBlockAndWxsMetadataAreRecognized`；实际工程包含 2 个 `wxs` 用例 |
| CAP-004 | `qs` 支持 `module`、`src` | 由 262 Web Types 提供标签、属性和文档 | P2 | pass | 生成器属性断言与 `testSpecialWxmlElementsAndTemplateAttributesAreRecognized` |
| CAP-005 | `page-meta` 支持页面配置属性 | 由 262 Web Types 提供标签、属性和文档 | P1 | pass | `testSpecialWxmlElementsAndTemplateAttributesAreRecognized` |
| CAP-006 | `navigation-bar` 支持导航栏配置属性 | 由 262 Web Types 提供标签、属性和文档 | P1 | pass | `testSpecialWxmlElementsAndTemplateAttributesAreRecognized` |
| CAP-007 | WXML `template` 支持 `name`、`is`、`data` | 与上游 Vue `template` 定义合并且不覆盖 `scope` | P1 | pass | 生成器合并测试确认 `data/is/name/scope` 且只有一个 `template` 定义 |
| CAP-008 | MPX 顶层 `template`、`script`、`style` 语义不变 | 保留 `vue-file-top-elements` 三个顶层块 | P0 | pass | 非元素分区校验和不变；Gradle 61/61；Plugin Verifier `Compatible` |
