# Current Task

## 背景

用户需要一个 Forge 1.20.1 物价计算模组，参考旧 1.12.2 `valued_items` 的“基础价 + 规则图 + tooltip”思路，但不依赖 ProjectE，也不移植旧 OreDictionary 格式。

## 当前目标

完成 `0.2.1` 安全与正确性 follow-up 修复、服务实例拆分和生成规则事务写入，并进入运行期验证。

## 本次已完成

- 新建项目 `F:/mcplugins/mod/arc-value-calculator`。
- 实现 Forge `1.20.1 / 47.3.0` 模组 `arcvaluecalc`。
- 实现 item 固定价、tag 固定价、手写规则、自动规则生成、tooltip、命令和基础网络同步。
- 构建产物已复制到 ArcartX 1.20.1 客户端 `mods` 目录。
- 新建仓库知识库和 P/G/E 文档。
- 按运行截图反馈关闭未知价格 tooltip 默认显示；当前客户端配置也已改为 `showUnknown = false`。
- 实现高危与正确性修复：受限价格解析、安全配置读写、默认 seed 不复活、配方空槽跳过、多候选最低成本、服务端 NBT 命中键响应、客户端负缓存、手写规则稳定迭代。
- 根据首轮 follow-up review 修复：协议曾升级到 `2`、模组版本升级到 `0.2.0`、客户端 generation 不再自增、旧响应不污染 serverAvailable、item-only 响应清理精确 NBT pending、请求限流和 NBT 上限、手写/生成规则全局收敛、生成规则路径 containment、`item_values.json` NBT 解析校验、标签价冲突取最低、低于 `0.0001` 的正数推导不归零。
- 拆分 `ValueServices.server()` 与 `ValueServices.clientFallback()`，服务端命令/网络/生命周期使用权威实例，客户端 recipe 更新只刷新本地 fallback 实例。
- 自动生成规则目录改为同父级临时目录写入，成功后替换旧目录，失败时保留旧目录。
- tooltip 文案改为 `参考价格 : %s%s`，默认单位改为 `落叶币`，命令反馈同步使用“参考价格”措辞。
- 修复 P1/P2 follow-up：版本升级到 `0.2.1`；超大 NBT tooltip 查询安全退回 item-only；网络请求包只传受限 `ValueKey`；协议升级到 `3`；`/arcvalue export rules` 写出当前缓存生成规则；生成规则文件名保留合法 `.`/`-` 并检测碰撞；tooltip 本地 fallback 直接调用客户端 fallback 服务；README 明确多人必须双端安装。
- 修复 P0 标签价优先级：标签价先单独展开并取最低，再用 `putIfAbsent` 合入，保证明确 item 固定价不被标签价覆盖。

## 已确认事实

- `./gradlew.bat test` 通过。
- `./gradlew.bat build` 通过。
- 当前 1.20.1 不使用旧矿辞 OreDictionary，按 item tags 支持。
- `value_rule_generated` 可以覆盖，手写规则应放 `value_rule`。
- 无价格物品默认不显示 tooltip 提示；`/arcvalue get` 查询未知价格时仍会返回命令反馈。
- 坏价格 JSON 有效条目仍参与计算，但在修复坏条目前写配置类命令会拒绝覆盖文件。
- 服务端返回缺失价格后，客户端不再回退本地计算值。
- 服务端响应携带 requested/resolved key；客户端按 resolved key 缓存，并清理 requested 精确键的 pending 状态。
- 网络协议 `PROTOCOL = "3"`，旧协议客户端/服务端不应与当前版本混用。
- 单机同 JVM 下逻辑服务端和逻辑客户端不再共享同一个 `ValueService` 快照。

## 待验证点

- 启动客户端 -> 验证主菜单和进世界不崩。
- 执行 `/arcvalue reload` -> 验证价格数量明显多于初始 96 项。
- 查看木头/木板 tooltip -> 验证 `minecraft:logs` 和 `minecraft:planks` 标签价生效。
- 专服/多人环境 -> 验证服务端权威价格请求和 reload 通知。
- 多 NBT 物品 -> 验证服务端精确 NBT 价格不会被 item-only 缓存覆盖，item-only 响应不会留下精确 pending。
- 断线后连接另一个新启动服务器 -> 验证较低但合法的新服务器 generation 响应仍被接受。

## 当前结论

代码层 follow-up 修复、服务实例拆分和生成规则事务写入已完成单元测试与构建，当前进入运行期手测阶段。下一类高价值改动是机器配方 adapter、容器返还物/燃料成本和配方环强连通分量检测。

## 下一步

- 重启客户端 -> 验证: 进入游戏无崩溃。
- 执行 `/arcvalue reload` -> 验证: 聊天显示可用价格数量，有价格物品显示价格，无价格物品不显示价格行。
- 执行 `/arcvalue set 1e100000000` -> 验证: 命令拒绝且服务端不 OOM。
- 删除默认基础价后 reload -> 验证: 删除项不复活。
- 根据手测缺口补基础 tag 价或手写规则 -> 验证: 对应物品 tooltip 正确显示。

## 验证记录

- 2026-06-23：`./gradlew.bat test` 通过。
- 2026-06-23：`./gradlew.bat build` 通过。
- 2026-06-23：jar 已复制到 `G:/MC/game/ArcartXDev1201Client/.minecraft/versions/1.20.1-Forge_47.3.0/mods/arcvaluecalc-0.1.0.jar`。
- 2026-06-23：关闭未知价格 tooltip 默认显示后，`./gradlew.bat test` 和 `./gradlew.bat build` 通过，新 jar 已覆盖到当前客户端 mods 目录。
- 2026-06-23：高危与正确性修复后，`./gradlew.bat compileJava` 和 `./gradlew.bat test` 通过；`./gradlew.bat build` 待执行。
- 2026-06-23：follow-up review 修复后，`./gradlew.bat test` 和 `./gradlew.bat build` 通过；jar 已复制到 `G:/MC/game/ArcartXDev1201Client/.minecraft/versions/1.20.1-Forge_47.3.0/mods/arcvaluecalc-0.2.0.jar`，旧 `arcvaluecalc-0.1.0.jar` 已移除。
- 2026-06-23：服务端/客户端 fallback 服务拆分、生成规则临时目录替换写入后，`./gradlew.bat test` 和 `./gradlew.bat build` 通过；jar 已覆盖到 `G:/MC/game/ArcartXDev1201Client/.minecraft/versions/1.20.1-Forge_47.3.0/mods/arcvaluecalc-0.2.0.jar`。
- 2026-06-24：P1/P2 follow-up 修复后，`./gradlew.bat test` 和 `./gradlew.bat build` 通过；jar 已覆盖到 `G:/MC/game/ArcartXDev1201Client/.minecraft/versions/1.20.1-Forge_47.3.0/mods/arcvaluecalc-0.2.1.jar`。
