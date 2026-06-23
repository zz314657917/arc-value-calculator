# Arc Value Calculator 开发入口

## 项目定位

- Forge `1.20.1 / 47.3.0` 独立客户端/服务端物价计算模组。
- 核心目标是“基础价格 + 标签价格 + 配方推导 + tooltip + 管理命令”，不是交易系统或 ProjectE 玩法。
- 当前开发目录：`F:/mcplugins/mod/arc-value-calculator`。

## 上下文入口

- 开始开发前先读：`knowledge/00-start-here.md`。
- 续做当前任务先读：`knowledge/tasks/current-task.md`。
- 回看阶段历史读：`knowledge/tasks/timeline.md`。
- 架构与数据格式读：`knowledge/architecture.md`。
- 构建和验证命令读：`knowledge/build-and-verify.md`。

## 硬约束

- 默认简体中文沟通。
- 修改范围保持在本仓库；不要直接改其他模组仓库。
- 自动生成目录 `config/arcvaluecalc/value_rule_generated/` 由程序托管，用户手写规则只放 `config/arcvaluecalc/value_rule/`。
- 1.20.1 不使用旧 OreDictionary；统一按 item tag 支持，例如 `minecraft:logs`、`forge:ingots/iron`。
- 变更后至少运行 `./gradlew.bat test`；涉及打包或资源时运行 `./gradlew.bat build`。
