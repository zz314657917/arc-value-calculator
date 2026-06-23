## 2026-06-23 15:34 +08:00 - 服务实例拆分与生成规则事务写入

- 当前阶段：`0.2.0` 继续完善，服务端/客户端 fallback 状态隔离与生成规则落盘安全已完成。
- 本段重点：新增 `ValueServices.server()` / `clientFallback()` 分流；服务端命令、网络和生命周期使用权威实例；客户端 recipe 更新只刷新 fallback 实例；生成规则目录先写临时目录再替换旧目录。
- 已完成：移除单一静态 `ValueService.get()`；`RuleFileStore` 支持路径注入和临时目录替换；补充生成规则目录替换测试；README 与 architecture/current-task/build 文档已同步。
- 关键决策：保留 `ArcValueApi` 外部接口不变，内部按当前是否有服务端选择 server 或 client fallback；生成规则写入失败时保留旧目录，不再先删后写。
- 验证记录：`./gradlew.bat test` 通过；`./gradlew.bat build` 通过；新版 `arcvaluecalc-0.2.0.jar` 已覆盖到 ArcartX 1.20.1 客户端 mods 目录。
- 遗留问题：仍未实际启动客户端或专服；机器配方 adapter、容器返还物/燃料成本、SCC 增益循环检测仍未实现。
- 下一步：提交并推送；重启客户端执行 `/arcvalue reload`；多人环境验证服务端权威值、NBT 精确价格和 reload 同步。

## 2026-06-23 13:25 +08:00 - 0.2.0 follow-up review 修复

- 当前阶段：按 review 继续加固 `0.2.0`，代码、测试、构建和客户端 jar 部署已完成。
- 本段重点：修复网络协议版本、客户端 generation 状态机、手写/生成规则全局收敛、请求限流、NBT 上限、生成规则路径 containment 和标签价冲突策略。
- 已完成：协议升到 `2`；模组版本升到 `0.2.0`；响应包携带 requested/resolved key；item-only 响应清理精确 NBT pending；`item_values.json` NBT 加载解析校验；低于 `0.0001` 的正数推导不归零。
- 关键决策：本轮不做单机静态 `ValueService` 拆分、生成目录事务交换、构建插件固定版本和价格图/规则导出拆分，避免安全补丁扩大为架构重构。
- 验证记录：`./gradlew.bat test` 通过；`./gradlew.bat build` 通过；`arcvaluecalc-0.2.0.jar` 已复制到客户端 mods，旧 `0.1.0` jar 已移除；运行期烟测待执行。
- 遗留问题：单机服务端/客户端 fallback 共享静态服务、容器返还物/燃料成本、机器配方 adapter、SCC 增益循环检测、专服多人 NBT 行为仍需后续验证或重构。
- 下一步：提交并推送；重启客户端执行 `/arcvalue reload`；多人环境验证 NBT 精确价格和 generation 行为。

## 2026-06-23 11:50 +08:00 - 高危与正确性修复

- 当前阶段：首版进入安全与正确性加固，已完成代码级修复和单元测试。
- 本段重点：统一价格解析边界；配置读有效但禁写；修复默认 seed 复活、配方空槽、多候选、服务端 NBT 缓存和手写规则迭代。
- 已完成：新增价格解析、配置诊断/备份/原子写入、候选集合规则模型、服务端响应命中键和客户端有界状态缓存。
- 关键决策：坏配置有效条目继续参与计算但拒绝写回；同优先级规则取最低成本；暂不做 SCC 配方环检测和性能重构。
- 验证记录：`./gradlew.bat compileJava` 通过；`./gradlew.bat test` 通过；完整 build 和运行期烟测待执行。
- 遗留问题：容器返还物、燃料/加工成本、机器配方 adapter、配方环强连通分量检测仍未实现；专服 NBT 网络行为需手测。
- 下一步：执行 `./gradlew.bat build`；覆盖客户端 jar；手测危险价格、坏 JSON 禁写、默认价不复活和 NBT 物品 tooltip。

## 2026-06-23 10:57 +08:00 - 关闭未知价格 tooltip 默认显示

- 当前阶段：首版运行期细节调整，减少无价格物品 tooltip 干扰。
- 本段重点：将 `showUnknown` 默认值改为 `false`；同步 README、知识库和当前客户端已生成配置。
- 已完成：源码默认配置、README、`knowledge/00-start-here.md`、`knowledge/build-and-verify.md`、`current-task.md` 已更新；新版 jar 已覆盖到 ArcartX 1.20.1 客户端 mods 目录。
- 关键决策：无价格物品默认不显示 tooltip 行；`/arcvalue get` 对未知价格仍保留命令反馈。
- 验证记录：`./gradlew.bat test` 通过；`./gradlew.bat build` 通过；客户端 `arcvaluecalc-common.toml` 已改为 `showUnknown = false`。
- 遗留问题：仍需重启客户端确认截图中的红色未知价格行消失；专服同步仍未烟测。
- 下一步：重启客户端并悬停未知价格物品；验证无价格 tooltip 不再出现；继续检查有价格物品仍显示 `价值：xx 枫币`。

## 2026-06-23 01:25 +08:00 - 首版 MVP 与知识库初始化

- 当前阶段：首版物价计算模组已实现，进入运行期手测与规则完善。
- 本段重点：新建 Forge 1.20.1 模组；实现 item/tag 价、规则推导、tooltip、命令；建立仓库知识库和时间轴。
- 已完成：`./gradlew.bat test`、`./gradlew.bat build` 通过；jar 已部署到 ArcartX 1.20.1 客户端 mods 目录。
- 关键决策：1.20.1 不使用旧 OreDictionary，统一按 item tag 支持；`value_rule_generated` 由程序托管；`/arcvalue set` 默认按物品 ID 写价。
- 验证记录：构建和单元测试通过；尚未完成客户端启动和专服联机烟测。
- 遗留问题：机器配方 adapter 未实现；多候选 Ingredient 自动规则仍是近似处理；服务端权威值需专服验证。
- 下一步：重启客户端并执行 `/arcvalue reload`；检查木头/木板 tooltip；根据缺口补 tag 价或手写规则。
