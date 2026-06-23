# Arc Value Calculator Spec

Forge 1.20.1 item value calculator using decimal prices. It provides manual fixed values, manual value rules, generated recipe rules, tooltip display, commands, and optional server-authoritative values.

## Decisions

- Values are decimals displayed with two fraction digits.
- Tooltip format is `价值：<value> <unit>`, with configurable unit.
- Unknown items do not show tooltip by default.
- `/arcvalue set` writes fixed values by item id only.
- Generated rules may be overwritten on recalculation.
- Server values are preferred when available; otherwise clients keep local display.
- First release covers common vanilla recipe types only.
- `value_rule_generated` is program-owned and may be deleted/recreated on reload. Hand-written rules must live under `value_rule`.
- Generated rules approximate multi-choice ingredients by selecting one concrete item candidate in v1. Use manual rules for exact tag semantics.
