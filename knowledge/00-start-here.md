# Arc Value Calculator 知识入口

## 快速结论

- 这是 Forge `1.20.1 / 47.3.0` 物价计算模组。
- 模组 ID：`arcvaluecalc`。
- 主包名：`com.liangmu.arcvaluecalc`。
- 运行配置目录：`config/arcvaluecalc/`。

## 当前事实源

- 当前任务快照：`knowledge/tasks/current-task.md`
- 阶段时间轴：`knowledge/tasks/timeline.md`
- 当前重点：`knowledge/05-current-focus.md`
- 架构说明：`knowledge/architecture.md`
- 构建验证：`knowledge/build-and-verify.md`

## 关键行为

- 固定物品价：`config/arcvaluecalc/item_values.json`
- 固定标签价：`config/arcvaluecalc/tag_values.json`
- 手写规则：`config/arcvaluecalc/value_rule/**/*.json`
- 自动生成规则：`config/arcvaluecalc/value_rule_generated/**/*.json`
- tooltip 默认显示两位小数和单位，未知价格默认不显示。

## 当前已知边界

- 第一版只覆盖普通配方类型，不适配 Mekanism、GTCEu、匠魂、植物魔法等机器配方。
- 自动生成规则对多候选输入仍是近似处理；需要精确定价时优先写 tag 价或手写规则。
