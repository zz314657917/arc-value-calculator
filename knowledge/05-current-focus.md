# 当前重点

## 当前阶段

首版 MVP 已实现并能构建，正在进入运行期手测和规则完善阶段。

## 本轮重点

- 已支持 item 固定价、tag 固定价、手写规则、自动规则、tooltip、命令、客户端本地计算和服务端值请求。
- 当前版本已提升到 `0.2.0`，网络协议版本为 `2`。
- 本轮重点是修复协议兼容、客户端 generation、规则全局收敛、请求限流、NBT 上限和标签最低价策略。
- `arcvaluecalc-0.2.0.jar` 已部署到 ArcartX 1.20.1 客户端 mods 目录，当前下一步是重启客户端执行 `/arcvalue reload` 检查 tooltip 和命令行为。

## 近期不要做

- 不要把机器配方一次性全适配进首版。
- 不要把手写规则写进 `value_rule_generated`。
- 不要恢复旧 1.12.2 OreDictionary JSON 格式。
- 不要在本轮把单机客户端 fallback 与服务端权威 `ValueService` 拆分成大重构；这是后续架构任务。
