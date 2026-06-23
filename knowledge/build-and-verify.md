# 构建与验证

## 环境

- Java：17
- Forge：`1.20.1-47.3.0`
- Gradle wrapper：仓库内 `gradlew.bat`
- 本机 JDK 路径已在 `gradle.properties` 指定：
  `C:/Program Files/Microsoft/jdk-17.0.10.7-hotspot`

## 常用命令

```powershell
./gradlew.bat test
./gradlew.bat build
```

## 已验证命令

- `./gradlew.bat test`：通过。
- `./gradlew.bat build`：通过，产物 `build/libs/arcvaluecalc-0.2.0.jar`。
- 已部署到客户端：`G:/MC/game/ArcartXDev1201Client/.minecraft/versions/1.20.1-Forge_47.3.0/mods/arcvaluecalc-0.2.0.jar`。
- 客户端 mods 目录旧版 `arcvaluecalc-0.1.0.jar` 已移除。
- 2026-06-23 15:34：拆分服务端/客户端 fallback 服务并改造生成规则事务写入后，`./gradlew.bat test`、`./gradlew.bat build` 通过；新版 `arcvaluecalc-0.2.0.jar` 已覆盖到客户端 mods 目录。

## 手动验证清单

- 重启客户端确认主菜单不崩。
- 进世界执行 `/arcvalue reload`。
- 手持木头、木板、钻石、铁粒执行 `/arcvalue get`。
- 检查 tooltip：
  - 有价格：`价值：0.04 枫币`
  - 无价格：默认不显示价格行。
- 执行：
  - `/arcvalue set 0.45`
  - `/arcvalue set 1e100000000` 应被拒绝
  - `/arcvalue remove`
  - `/arcvalue settag minecraft:logs 0.04`
  - `/arcvalue removetag minecraft:logs`
  - `/arcvalue export values`
  - `/arcvalue export rules`
- 手动修改 `item_values.json` 删除默认项后执行 `/arcvalue reload`，确认默认项不会自动复活。
- 临时加入坏价格条目后执行修改命令，确认命令拒绝覆盖配置且日志指出数组索引。
- 使用两个同物品不同 NBT 的物品连接专服测试 tooltip，确认精确 NBT 价格和 item-only 响应不会互相污染缓存。
- 断线后连接另一个新启动服务器，确认 generation 较低的合法响应仍会被接受。
