# MPX WebStorm 262 原生标签阻塞记录

| Blocker ID | 能力 | 等级 | 状态 | 描述 | 关闭证据 |
| --- | --- | --- | --- | --- | --- |
| BLK-001 | 全量 Gradle 验证 | P1 | resolved | Java/Gradle 访问 JetBrains 下载域名时 TLS 握手间歇失败 | 使用官方 SHA-1 校验的制品填充隔离构建缓存后，`test`、`check`、`buildPlugin`、结构校验与 Plugin Verifier 全部通过；未修改仓库依赖配置 |
