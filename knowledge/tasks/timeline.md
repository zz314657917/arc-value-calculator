## 2026-06-23 01:25 +08:00 - 首版 MVP 与知识库初始化

- 当前阶段：首版物价计算模组已实现，进入运行期手测与规则完善。
- 本段重点：新建 Forge 1.20.1 模组；实现 item/tag 价、规则推导、tooltip、命令；建立仓库知识库和时间轴。
- 已完成：`./gradlew.bat test`、`./gradlew.bat build` 通过；jar 已部署到 ArcartX 1.20.1 客户端 mods 目录。
- 关键决策：1.20.1 不使用旧 OreDictionary，统一按 item tag 支持；`value_rule_generated` 由程序托管；`/arcvalue set` 默认按物品 ID 写价。
- 验证记录：构建和单元测试通过；尚未完成客户端启动和专服联机烟测。
- 遗留问题：机器配方 adapter 未实现；多候选 Ingredient 自动规则仍是近似处理；服务端权威值需专服验证。
- 下一步：重启客户端并执行 `/arcvalue reload`；检查木头/木板 tooltip；根据缺口补 tag 价或手写规则。
