# 架构说明

## 模块分层

- `api`：公开查询接口 `ArcValueApi`。
- `model`：`ValueKey`、`ValueRule`、`RuleIngredient`、`ValueSource` 等数据结构。
- `storage`：读取和写入 `item_values.json`、`tag_values.json`、规则文件。
- `service`：配方规则生成、tag 索引、价格计算、运行时缓存。
- `command`：`/arcvalue` 管理命令。
- `tooltip`：客户端 tooltip 显示。
- `network`：客户端向服务端请求价格、服务端 reload 通知；当前协议版本为 `2`，客户端和服务端严格相等匹配。
- `client`：客户端 recipe 更新后本地重算、服务端值缓存；服务端值缓存按 generation 接受响应，并清理 item-only 响应对应的精确 NBT pending key。
- `service.PriceParser`：所有命令、JSON 和网络价格字符串的统一安全解析入口。
- `service.ValueServices`：维护服务端权威 `ValueService` 和客户端 fallback `ValueService` 两个独立实例，避免单机同 JVM 下逻辑服务端和逻辑客户端互相覆盖快照。

## 数据优先级

1. `item_values.json` 固定物品价最高。
2. `tag_values.json` 展开为对应标签内物品的基础价，但不覆盖已有 item 价；多个标签命中同一物品时取最低价。
3. `value_rule/` 手写规则优先于自动规则。
4. `value_rule_generated/` 自动规则用于普通配方推导。

价格文件加载采用“读有效但禁写”策略：坏条目会生成诊断日志，有效条目仍参与计算；存在诊断错误时，写配置类命令拒绝覆盖原文件。保存使用同目录临时文件、`.bak` 和原子替换。

内部计算保留高精度小数，只在 tooltip、命令输出和导出时格式化；低于 `0.0001` 的正数推导值会抬到 `0.0001`，避免零成本链。

自动生成规则目录写入采用同父级临时目录：先完整写入临时目录，成功后替换旧目录；失败时清理临时目录并保留旧生成规则目录。

## Tag 约定

- 1.20.1 使用 item tags，不使用旧 OreDictionary。
- 示例：
  - `minecraft:logs`
  - `minecraft:planks`
  - `forge:ingots/iron`
  - `forge:nuggets/gold`

## 已知限制

- 多候选 `Ingredient` 已保留候选集合并按最低已知成本估值。
- NBT 价格能力保留在数据结构中，但 `/arcvalue set` 默认只按物品 ID 写价。
- 容器返还物、燃料成本、加工成本、机器配方 adapter 和配方环强连通分量检测尚未实现。
- 专服网络同步、NBT 精确价格和断线重连 generation 行为仍需实际多人环境烟测。
