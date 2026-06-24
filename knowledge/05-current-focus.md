# 当前重点

## 当前阶段

首版 MVP 已实现并能构建，正在进入运行期手测和规则完善阶段。

## 本轮重点

- 已支持 item 固定价、tag 固定价、手写规则、自动规则、tooltip、命令、客户端本地计算和服务端值请求。
- 当前版本已提升到 `0.2.1`，网络协议版本为 `3`。
- 服务端权威价格服务和客户端 fallback 服务已拆分，自动生成规则目录已改为临时目录写入后替换。
- 网络请求包已改为只传 `ValueKey`，不再发送完整 `ItemStack`；超大 NBT tooltip 查询会安全退回 item-only。
- 当前默认显示文案为 `参考价格 : xx落叶币`。
- 当前下一步是运行 `./gradlew.bat build`、部署新 jar，并重启客户端执行 `/arcvalue reload` 检查 tooltip 和命令行为。

## 近期不要做

- 不要把机器配方一次性全适配进首版。
- 不要把手写规则写进 `value_rule_generated`。
- 不要恢复旧 1.12.2 OreDictionary JSON 格式。
