# 架构说明

## 模块分层

- `api`：公开查询接口 `ArcValueApi`。
- `model`：`ValueKey`、`ValueRule`、`RuleIngredient`、`ValueSource` 等数据结构。
- `storage`：读取和写入 `item_values.json`、`tag_values.json`、规则文件。
- `service`：配方规则生成、tag 索引、价格计算、运行时缓存。
- `command`：`/arcvalue` 管理命令。
- `tooltip`：客户端 tooltip 显示。
- `network`：客户端向服务端请求价格、服务端 reload 通知。
- `client`：客户端 recipe 更新后本地重算、服务端值缓存。

## 数据优先级

1. `item_values.json` 固定物品价最高。
2. `tag_values.json` 展开为对应标签内物品的基础价，但不覆盖已有 item 价。
3. `value_rule/` 手写规则优先于自动规则。
4. `value_rule_generated/` 自动规则用于普通配方推导。

## Tag 约定

- 1.20.1 使用 item tags，不使用旧 OreDictionary。
- 示例：
  - `minecraft:logs`
  - `minecraft:planks`
  - `forge:ingots/iron`
  - `forge:nuggets/gold`

## 已知限制

- 多候选 `Ingredient` 的自动规则仍是近似处理；需要精确语义时应写 tag 价或手写规则。
- NBT 价格能力保留在数据结构中，但 `/arcvalue set` 默认只按物品 ID 写价。
- 机器配方 adapter 尚未实现。
