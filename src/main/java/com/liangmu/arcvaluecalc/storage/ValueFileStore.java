package com.liangmu.arcvaluecalc.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.liangmu.arcvaluecalc.model.ValueKey;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class ValueFileStore {
    public Map<ValueKey, BigDecimal> loadManualValues() {
        ensureSeedFile();
        Map<ValueKey, BigDecimal> values = new LinkedHashMap<>();
        Path path = ValuePaths.itemValues();
        if (!Files.exists(path)) {
            return values;
        }
        try {
            JsonElement root = JsonUtil.read(path);
            if (!root.isJsonArray()) {
                return values;
            }
            for (JsonElement element : root.getAsJsonArray()) {
                JsonObject object = element.getAsJsonObject();
                ValueKey key = readKey(object);
                BigDecimal value = object.get("value").getAsBigDecimal();
                values.put(key, value);
            }
        } catch (Exception ignored) {
            // A bad config should not crash the game. Commands/export can rewrite it.
        }
        return values;
    }

    public Map<ResourceLocation, BigDecimal> loadTagValues() {
        ensureTagSeedFile();
        Map<ResourceLocation, BigDecimal> values = new LinkedHashMap<>();
        Path path = ValuePaths.tagValues();
        if (!Files.exists(path)) {
            return values;
        }
        try {
            JsonElement root = JsonUtil.read(path);
            if (!root.isJsonArray()) {
                return values;
            }
            for (JsonElement element : root.getAsJsonArray()) {
                JsonObject object = element.getAsJsonObject();
                ResourceLocation tag = new ResourceLocation(object.get("tag").getAsString());
                BigDecimal value = object.get("value").getAsBigDecimal();
                values.put(tag, value);
            }
        } catch (Exception ignored) {
        }
        return values;
    }

    public void saveManualValues(Map<ValueKey, BigDecimal> values) throws IOException {
        JsonArray array = new JsonArray();
        values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey((a, b) -> a.toString().compareTo(b.toString())))
                .forEach(entry -> {
                    JsonObject object = writeKey(entry.getKey());
                    object.addProperty("value", entry.getValue().stripTrailingZeros().toPlainString());
                    array.add(object);
                });
        JsonUtil.write(ValuePaths.itemValues(), array);
    }

    public void setManualValue(ValueKey key, BigDecimal value) throws IOException {
        Map<ValueKey, BigDecimal> values = loadManualValues();
        values.put(key, value);
        saveManualValues(values);
    }

    public boolean removeManualValue(ValueKey key) throws IOException {
        Map<ValueKey, BigDecimal> values = loadManualValues();
        boolean removed = values.remove(key) != null;
        saveManualValues(values);
        return removed;
    }

    public void setTagValue(ResourceLocation tag, BigDecimal value) throws IOException {
        Map<ResourceLocation, BigDecimal> values = loadTagValues();
        values.put(tag, value);
        saveTagValues(values);
    }

    public boolean removeTagValue(ResourceLocation tag) throws IOException {
        Map<ResourceLocation, BigDecimal> values = loadTagValues();
        boolean removed = values.remove(tag) != null;
        saveTagValues(values);
        return removed;
    }

    public void saveTagValues(Map<ResourceLocation, BigDecimal> values) throws IOException {
        JsonArray array = new JsonArray();
        values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey((a, b) -> a.toString().compareTo(b.toString())))
                .forEach(entry -> {
                    JsonObject object = new JsonObject();
                    object.addProperty("tag", entry.getKey().toString());
                    object.addProperty("value", entry.getValue().stripTrailingZeros().toPlainString());
                    array.add(object);
                });
        JsonUtil.write(ValuePaths.tagValues(), array);
    }

    private ValueKey readKey(JsonObject object) {
        String item = object.get("item").getAsString();
        String nbt = object.has("nbt") ? object.get("nbt").getAsString() : null;
        return new ValueKey(new ResourceLocation(item), nbt);
    }

    private JsonObject writeKey(ValueKey key) {
        JsonObject object = new JsonObject();
        object.addProperty("item", key.item().toString());
        if (key.nbt() != null) {
            object.addProperty("nbt", key.nbt());
        }
        return object;
    }

    private void ensureSeedFile() {
        Path path = ValuePaths.itemValues();
        if (Files.exists(path)) {
            mergeMissingSeeds(path);
            return;
        }
        JsonArray array = new JsonArray();
        addDefaultSeeds(array);
        try {
            JsonUtil.write(path, array);
        } catch (IOException ignored) {
        }
    }

    private void ensureTagSeedFile() {
        Path path = ValuePaths.tagValues();
        if (Files.exists(path)) {
            return;
        }
        JsonArray array = new JsonArray();
        tagSeed(array, "minecraft:logs", "0.04");
        tagSeed(array, "minecraft:planks", "0.01");
        tagSeed(array, "forge:nuggets/iron", "0.01");
        tagSeed(array, "forge:nuggets/gold", "0.02");
        try {
            JsonUtil.write(path, array);
        } catch (IOException ignored) {
        }
    }

    private void mergeMissingSeeds(Path path) {
        try {
            JsonElement root = JsonUtil.read(path);
            if (!root.isJsonArray()) {
                return;
            }
            JsonArray array = root.getAsJsonArray();
            Set<String> existing = new HashSet<>();
            for (JsonElement element : array) {
                if (element.isJsonObject()) {
                    JsonObject object = element.getAsJsonObject();
                    if (object.has("item") && !object.has("nbt")) {
                        existing.add(object.get("item").getAsString());
                    }
                }
            }
            JsonArray seeds = new JsonArray();
            addDefaultSeeds(seeds);
            boolean changed = false;
            for (JsonElement element : seeds) {
                JsonObject object = element.getAsJsonObject();
                if (existing.add(object.get("item").getAsString())) {
                    array.add(object);
                    changed = true;
                }
            }
            if (changed) {
                JsonUtil.write(path, array);
            }
        } catch (Exception ignored) {
        }
    }

    private void addDefaultSeeds(JsonArray array) {
        seed(array, "minecraft:stone", "0.02");
        seed(array, "minecraft:cobblestone", "0.02");
        seed(array, "minecraft:dirt", "0.01");
        seed(array, "minecraft:sand", "0.01");
        seed(array, "minecraft:gravel", "0.01");
        seed(array, "minecraft:coal", "0.05");
        seed(array, "minecraft:iron_nugget", "0.01");
        seed(array, "minecraft:gold_nugget", "0.02");
        seed(array, "minecraft:diamond", "0.45");
        seed(array, "minecraft:redstone", "0.02");
        seed(array, "minecraft:emerald", "1.80");
        seed(array, "minecraft:stick", "0.01");
        seed(array, "minecraft:white_wool", "0.10");
        seed(array, "minecraft:obsidian", "0.10");
        seed(array, "minecraft:sugar_cane", "0.01");
        seed(array, "minecraft:quartz", "0.12");
        seed(array, "minecraft:oak_log", "0.04");
        seed(array, "minecraft:spruce_log", "0.04");
        seed(array, "minecraft:birch_log", "0.04");
        seed(array, "minecraft:jungle_log", "0.04");
        seed(array, "minecraft:acacia_log", "0.04");
        seed(array, "minecraft:dark_oak_log", "0.04");
        seed(array, "minecraft:mangrove_log", "0.04");
        seed(array, "minecraft:cherry_log", "0.04");
        seed(array, "minecraft:crimson_stem", "0.04");
        seed(array, "minecraft:warped_stem", "0.04");
        seed(array, "minecraft:bamboo_block", "0.04");
    }

    private void seed(JsonArray array, String item, String value) {
        JsonObject object = new JsonObject();
        object.addProperty("item", item);
        object.addProperty("value", value);
        array.add(object);
    }

    private void tagSeed(JsonArray array, String tag, String value) {
        JsonObject object = new JsonObject();
        object.addProperty("tag", tag);
        object.addProperty("value", value);
        array.add(object);
    }
}
