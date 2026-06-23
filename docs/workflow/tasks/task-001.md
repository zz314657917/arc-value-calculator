# task-001: First Playable Arc Value Calculator

## Goal

Implement the first playable Forge 1.20.1 mod with config, rule generation, value calculation, tooltip, commands, and tests.

## Success Criteria

- `./gradlew.bat test` passes.
- `./gradlew.bat build` produces a reobfuscated jar.
- Manual values override manual rules; manual rules override generated rules.
- Tooltip shows two decimal places and configured unit.
- `/arcvalue get/set/remove/reload/export` are registered with OP permission config.

## Allowed Paths

- `src/**`
- `docs/workflow/**`
- Gradle project files

## Denied Paths

- Other mod repositories
- Game instance files, except optional final jar copy after build
