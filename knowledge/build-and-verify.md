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
- `./gradlew.bat build`：通过，产物 `build/libs/arcvaluecalc-0.1.0.jar`。

## 手动验证清单

- 重启客户端确认主菜单不崩。
- 进世界执行 `/arcvalue reload`。
- 手持木头、木板、钻石、铁粒执行 `/arcvalue get`。
- 检查 tooltip：
  - 有价格：`价值：0.04 枫币`
  - 无价格：默认不显示价格行。
- 执行：
  - `/arcvalue set 0.45`
  - `/arcvalue remove`
  - `/arcvalue settag minecraft:logs 0.04`
  - `/arcvalue removetag minecraft:logs`
  - `/arcvalue export values`
  - `/arcvalue export rules`
