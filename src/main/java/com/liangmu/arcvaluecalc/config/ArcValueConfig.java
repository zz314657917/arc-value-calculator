package com.liangmu.arcvaluecalc.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ArcValueConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue SHOW_TOOLTIP;
    public static final ForgeConfigSpec.BooleanValue SHOW_UNKNOWN;
    public static final ForgeConfigSpec.BooleanValue PREFER_SERVER_VALUES;
    public static final ForgeConfigSpec.BooleanValue GENERATE_RULE_FILES;
    public static final ForgeConfigSpec.IntValue MAX_ITERATIONS;
    public static final ForgeConfigSpec.IntValue ADMIN_PERMISSION_LEVEL;
    public static final ForgeConfigSpec.ConfigValue<String> VALUE_UNIT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("display");
        SHOW_TOOLTIP = builder.comment("Show item value in tooltips.")
                .define("showTooltip", true);
        SHOW_UNKNOWN = builder.comment("Show an unknown-value tooltip line for items without calculated values.")
                .define("showUnknown", false);
        VALUE_UNIT = builder.comment("Displayed unit after the value, for example 落叶币.")
                .define("valueUnit", "落叶币");
        builder.pop();

        builder.push("sync");
        PREFER_SERVER_VALUES = builder.comment("Prefer server-authoritative values when the server has this mod installed.")
                .define("preferServerValues", true);
        builder.pop();

        builder.push("calculation");
        GENERATE_RULE_FILES = builder.comment("Overwrite generated recipe rule JSON files during recalculation.")
                .define("generateRuleFiles", true);
        MAX_ITERATIONS = builder.comment("Maximum relaxation iterations for value calculation.")
                .defineInRange("maxIterations", 128, 1, 4096);
        builder.pop();

        builder.push("commands");
        ADMIN_PERMISSION_LEVEL = builder.comment("Permission level required for mutating arcvalue commands.")
                .defineInRange("adminPermissionLevel", 2, 0, 4);
        builder.pop();

        SPEC = builder.build();
    }

    private ArcValueConfig() {
    }
}
