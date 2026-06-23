# 当前重点

## 当前阶段

首版 MVP 已实现并能构建，正在进入运行期手测和规则完善阶段。

## 本轮重点

- 已支持 item 固定价、tag 固定价、手写规则、自动规则、tooltip、命令、客户端本地计算和服务端值请求。
- 已部署 jar 到 `G:/MC/game/ArcartXDev1201Client/.minecraft/versions/1.20.1-Forge_47.3.0/mods/arcvaluecalc-0.1.0.jar`。
- 当前下一步是重启客户端，执行 `/arcvalue reload`，检查木头/木板、基础矿物和命令行为。

## 近期不要做

- 不要把机器配方一次性全适配进首版。
- 不要把手写规则写进 `value_rule_generated`。
- 不要恢复旧 1.12.2 OreDictionary JSON 格式。
