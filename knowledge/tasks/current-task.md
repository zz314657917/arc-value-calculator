# Current Task

## 背景

用户需要一个 Forge 1.20.1 物价计算模组，参考旧 1.12.2 `valued_items` 的“基础价 + 规则图 + tooltip”思路，但不依赖 ProjectE，也不移植旧 OreDictionary 格式。

## 当前目标

完成首版 MVP 的运行期验证，并继续完善标签价、基础价和普通配方推导体验。

## 本次已完成

- 新建项目 `F:/mcplugins/mod/arc-value-calculator`。
- 实现 Forge `1.20.1 / 47.3.0` 模组 `arcvaluecalc`。
- 实现 item 固定价、tag 固定价、手写规则、自动规则生成、tooltip、命令和基础网络同步。
- 构建产物已复制到 ArcartX 1.20.1 客户端 `mods` 目录。
- 新建仓库知识库和 P/G/E 文档。

## 已确认事实

- `./gradlew.bat test` 通过。
- `./gradlew.bat build` 通过。
- 当前 1.20.1 不使用旧矿辞 OreDictionary，按 item tags 支持。
- `value_rule_generated` 可以覆盖，手写规则应放 `value_rule`。

## 待验证点

- 启动客户端 -> 验证主菜单和进世界不崩。
- 执行 `/arcvalue reload` -> 验证价格数量明显多于初始 96 项。
- 查看木头/木板 tooltip -> 验证 `minecraft:logs` 和 `minecraft:planks` 标签价生效。
- 专服/多人环境 -> 验证服务端权威价格请求和 reload 通知。

## 当前结论

代码层首版已完成，当前进入运行期手测阶段。下一类高价值改动是改善 tag 语义、多候选 Ingredient 处理和机器配方 adapter。

## 下一步

- 重启客户端 -> 验证: 进入游戏无崩溃。
- 执行 `/arcvalue reload` -> 验证: 聊天显示可用价格数量，tooltip 有价格或暂无价格提示。
- 根据手测缺口补基础 tag 价或手写规则 -> 验证: 对应物品 tooltip 正确显示。

## 验证记录

- 2026-06-23：`./gradlew.bat test` 通过。
- 2026-06-23：`./gradlew.bat build` 通过。
- 2026-06-23：jar 已复制到 `G:/MC/game/ArcartXDev1201Client/.minecraft/versions/1.20.1-Forge_47.3.0/mods/arcvaluecalc-0.1.0.jar`。
