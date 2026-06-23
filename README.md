# Arc Value Calculator

Forge 1.20.1 item value calculator mod.

这是一个轻量物价计算模组，用来给物品显示类似 EMC 的“价值”，但不依赖 ProjectE，也不加入兑换、交易或货币玩法。它适合整合包或服务器维护一套基础物价，并通过普通配方自动推导更多物品价格。

## 功能

- 物品 tooltip 显示价格，例如 `价值：0.45 枫币`。
- 支持固定物品价格：`config/arcvaluecalc/item_values.json`。
- 支持固定标签价格：`config/arcvaluecalc/tag_values.json`。
- 支持手写规则：`config/arcvaluecalc/value_rule/**/*.json`。
- 支持自动生成普通配方规则：`config/arcvaluecalc/value_rule_generated/**/*.json`。
- 支持 `/arcvalue` 命令查询、设置、删除、重载和导出。
- 客户端单机可本地计算；多人服务器优先使用服务端权威价格。
- 使用小数价格体系，不使用 EMC 大整数。
- 价格输入带安全边界：不支持指数，最多 15 位整数和 4 位小数，最大 `1000000000000`。
- 网络协议版本：`2`，客户端和服务端必须协议严格一致。

## 兼容版本

- Minecraft: `1.20.1`
- Forge: `47.3.0`
- Java: `17`
- Mod ID: `arcvaluecalc`

## 安装

1. 下载或构建 `arcvaluecalc-0.2.0.jar`。
2. 放入客户端或服务端的 `mods` 目录。
3. 启动游戏或服务器。
4. 首次加载后会生成配置目录：

```text
config/arcvaluecalc/
```

多人服务器建议客户端和服务端都安装。本模组会在连接服务器时优先请求服务端价格。

## 首次使用

进入世界后执行：

```text
/arcvalue reload
```

然后把鼠标移动到物品上查看 tooltip。

默认会生成基础价格文件和标签价格文件。自动规则文件会在加载配方后写入：

```text
config/arcvaluecalc/value_rule_generated/
```

这个目录由程序托管，可以被重载覆盖。需要自己长期维护的规则请写到：

```text
config/arcvaluecalc/value_rule/
```

自动生成目录会先写入同父级临时目录，全部规则写入成功后再替换旧目录；写入失败会保留旧生成目录。

## 价格优先级

价格来源按下面顺序生效：

1. `item_values.json` 固定物品价。
2. `tag_values.json` 固定标签价，展开到标签内物品，但不覆盖已有物品价；多个标签命中同一物品时取最低价格。
3. `value_rule/` 手写规则。
4. `value_rule_generated/` 自动生成规则。

手写配置永远优先于自动生成结果。

`item_values.json` 和 `tag_values.json` 只在文件不存在时写入默认内容。文件存在后，重启或 `/arcvalue reload` 不会把被删除的默认基础价重新补回。

## 配置文件

Forge 通用配置：

```text
config/arcvaluecalc-common.toml
```

常用配置项：

```toml
showTooltip = true
showUnknown = false
valueUnit = "枫币"
preferServerValues = true
generateRuleFiles = true
maxIterations = 128
adminPermissionLevel = 2
```

说明：

- `showTooltip`：是否显示价格 tooltip。
- `showUnknown`：未知价格是否显示红色 `该物品暂无价格。`，默认不显示。
- `valueUnit`：价格单位显示文本。
- `preferServerValues`：多人服务器优先使用服务端价格。
- `generateRuleFiles`：是否落盘自动生成规则。
- `maxIterations`：循环配方推导的最大迭代次数。
- `adminPermissionLevel`：修改类命令所需权限等级。

## 固定物品价格

文件：

```text
config/arcvaluecalc/item_values.json
```

格式：

```json
[
  {
    "item": "minecraft:diamond",
    "value": "0.45"
  },
  {
    "item": "minecraft:stone",
    "value": "0.02"
  }
]
```

可选 NBT 匹配：

```json
[
  {
    "item": "minecraft:diamond_sword",
    "nbt": "{Damage:0}",
    "value": "3.50"
  }
]
```

`/arcvalue set` 第一版只按物品 ID 写入固定价，不会自动写 NBT 价格。

价格字段必须写成字符串形式，例如 `"0.45"`。非法价格或非法 SNBT 会被记录到日志；有效条目仍参与计算，但在修复坏条目前，`set/remove/settag/removetag/export values` 会拒绝覆盖配置文件。

## 固定标签价格

Minecraft 1.20.1 不使用旧版 OreDictionary。这个模组按 item tag 支持类似“矿辞”的需求。

文件：

```text
config/arcvaluecalc/tag_values.json
```

格式：

```json
[
  {
    "tag": "minecraft:logs",
    "value": "0.04"
  },
  {
    "tag": "minecraft:planks",
    "value": "0.01"
  },
  {
    "tag": "forge:nuggets/iron",
    "value": "0.01"
  }
]
```

命令也支持带 `#` 或不带 `#`：

```text
/arcvalue settag minecraft:logs 0.04
/arcvalue settag #minecraft:logs 0.04
/arcvalue removetag minecraft:logs
```

## 手写规则

手写规则放到：

```text
config/arcvaluecalc/value_rule/
```

示例：

```json
{
  "inputs": [
    {
      "item": "minecraft:iron_ingot",
      "count": 1
    }
  ],
  "outputs": [
    {
      "item": "minecraft:iron_nugget",
      "count": 9
    }
  ]
}
```

输入支持 `item`、`tag`、可选 `nbt`、`count`：

```json
{
  "inputs": [
    {
      "tag": "minecraft:logs",
      "count": 1
    }
  ],
  "outputs": [
    {
      "item": "minecraft:oak_planks",
      "count": 4
    }
  ]
}
```

输出支持 `item`、可选 `nbt`、`count`。

NBT 使用 1.20.1 SNBT 字符串，不兼容旧 1.12.2 `NBTTag` JSON 格式。

## 自动规则

当前自动生成覆盖常见普通配方：

- crafting
- smelting
- blasting
- smoking
- campfire_cooking
- stonecutting
- smithing_transform

自动规则会写入：

```text
config/arcvaluecalc/value_rule_generated/
```

未知输入不会参与本轮推导。循环配方通过迭代计算稳定最小值。

自动规则会跳过有序配方里的空槽。多候选输入会保留候选集合，并在计算时从已知候选中选择最低成本项。
内部计算使用高精度小数，只有 tooltip、命令输出和导出时格式化；低于 `0.0001` 的正数推导结果会抬到 `0.0001`，避免零成本链。

## 命令

查询手持物品价格：

```text
/arcvalue get
```

设置手持物品固定价格：

```text
/arcvalue set 0.45
```

删除手持物品固定价格：

```text
/arcvalue remove
```

设置标签价格：

```text
/arcvalue settag minecraft:logs 0.04
```

删除标签价格：

```text
/arcvalue removetag minecraft:logs
```

重载配置并重算：

```text
/arcvalue reload
```

导出自动规则或当前价格表：

```text
/arcvalue export rules
/arcvalue export values
```

`get` 可由玩家使用。`set`、`remove`、`settag`、`removetag`、`reload`、`export` 需要 `adminPermissionLevel` 配置指定的权限，默认 OP 2+。

## 开发构建

仓库路径：

```text
F:/mcplugins/mod/arc-value-calculator
```

构建要求：

- JDK 17
- Windows 下优先使用仓库内 `gradlew.bat`

命令：

```powershell
./gradlew.bat test
./gradlew.bat build
```

构建产物：

```text
build/libs/arcvaluecalc-0.2.0.jar
```

## 当前限制

- 第一版不适配 Mekanism、GTCEu、匠魂、植物魔法等机器配方。
- 多候选 `Ingredient` 已按候选最低成本估值；复杂语义仍建议写 tag 价或手写规则。
- NBT 定价能力存在于数据结构中，但命令只完整管理物品 ID 固定价和标签价。
- 专服网络同步需要继续做实际多人环境烟测。
- 仍未处理容器返还物、燃料成本、加工成本和配方环强连通分量检测。
- 服务端权威价格服务和客户端本地 fallback 服务已拆分；专服网络同步仍需要实际多人环境烟测。
