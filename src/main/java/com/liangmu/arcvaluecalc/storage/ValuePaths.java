package com.liangmu.arcvaluecalc.storage;

import com.liangmu.arcvaluecalc.ArcValueCalc;
import java.nio.file.Path;
import net.minecraftforge.fml.loading.FMLPaths;

public final class ValuePaths {
    private ValuePaths() {
    }

    public static Path root() {
        return FMLPaths.CONFIGDIR.get().resolve(ArcValueCalc.MOD_ID);
    }

    public static Path itemValues() {
        return root().resolve("item_values.json");
    }

    public static Path tagValues() {
        return root().resolve("tag_values.json");
    }

    public static Path manualRules() {
        return root().resolve("value_rule");
    }

    public static Path generatedRules() {
        return root().resolve("value_rule_generated");
    }

    public static Path exportDir() {
        return root().resolve("export");
    }
}
