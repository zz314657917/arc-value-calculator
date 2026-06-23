package com.liangmu.arcvaluecalc.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.liangmu.arcvaluecalc.ArcValueCalc;
import com.liangmu.arcvaluecalc.model.ValueKey;
import com.liangmu.arcvaluecalc.service.PriceParser;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;

public final class ValueFileStore {
    private final Path itemValuesPath;
    private final Path tagValuesPath;

    public ValueFileStore() {
        this(ValuePaths.itemValues(), ValuePaths.tagValues());
    }

    public ValueFileStore(Path itemValuesPath, Path tagValuesPath) {
        this.itemValuesPath = itemValuesPath;
        this.tagValuesPath = tagValuesPath;
    }

    public LoadResult<ValueKey, BigDecimal> loadManualValues() {
        ensureSeedFile();
        Path path = itemValuesPath;
        Map<ValueKey, BigDecimal> values = new LinkedHashMap<>();
        List<ConfigDiagnostic> diagnostics = new ArrayList<>();
        if (!Files.exists(path)) {
            return new LoadResult<>(values, diagnostics);
        }
        JsonArray array = readArray(path, diagnostics);
        if (array == null) {
            return new LoadResult<>(values, diagnostics);
        }
        for (int i = 0; i < array.size(); i++) {
            try {
                JsonObject object = requireObject(array.get(i), path, "$[" + i + "]");
                ValueKey key = readKey(object, path, "$[" + i + "]");
                BigDecimal value = readPrice(object, "value", path, "$[" + i + "]");
                values.put(key, value);
            } catch (IllegalArgumentException e) {
                addDiagnostic(diagnostics, path, "$[" + i + "]", e.getMessage());
            }
        }
        return new LoadResult<>(values, diagnostics);
    }

    public LoadResult<ResourceLocation, BigDecimal> loadTagValues() {
        ensureTagSeedFile();
        Path path = tagValuesPath;
        Map<ResourceLocation, BigDecimal> values = new LinkedHashMap<>();
        List<ConfigDiagnostic> diagnostics = new ArrayList<>();
        if (!Files.exists(path)) {
            return new LoadResult<>(values, diagnostics);
        }
        JsonArray array = readArray(path, diagnostics);
        if (array == null) {
            return new LoadResult<>(values, diagnostics);
        }
        for (int i = 0; i < array.size(); i++) {
            try {
                JsonObject object = requireObject(array.get(i), path, "$[" + i + "]");
                ResourceLocation tag = new ResourceLocation(requireString(object, "tag", path, "$[" + i + "]"));
                BigDecimal value = readPrice(object, "value", path, "$[" + i + "]");
                values.put(tag, value);
            } catch (Exception e) {
                addDiagnostic(diagnostics, path, "$[" + i + "]", e.getMessage());
            }
        }
        return new LoadResult<>(values, diagnostics);
    }

    public void saveManualValues(Map<ValueKey, BigDecimal> values) throws IOException {
        JsonArray array = new JsonArray();
        values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey((a, b) -> a.toString().compareTo(b.toString())))
                .forEach(entry -> {
                    JsonObject object = writeKey(entry.getKey());
                    object.addProperty("value", PriceParser.toPlainString(entry.getValue()));
                    array.add(object);
                });
        JsonUtil.writeAtomicWithBackup(itemValuesPath, array);
    }

    public void setManualValue(ValueKey key, BigDecimal value) throws IOException {
        LoadResult<ValueKey, BigDecimal> result = loadManualValues();
        ensureWritable(result);
        Map<ValueKey, BigDecimal> values = new LinkedHashMap<>(result.values());
        values.put(key, PriceParser.normalizeComputed(value));
        saveManualValues(values);
    }

    public boolean removeManualValue(ValueKey key) throws IOException {
        LoadResult<ValueKey, BigDecimal> result = loadManualValues();
        ensureWritable(result);
        Map<ValueKey, BigDecimal> values = new LinkedHashMap<>(result.values());
        boolean removed = values.remove(key) != null;
        saveManualValues(values);
        return removed;
    }

    public void setTagValue(ResourceLocation tag, BigDecimal value) throws IOException {
        LoadResult<ResourceLocation, BigDecimal> result = loadTagValues();
        ensureWritable(result);
        Map<ResourceLocation, BigDecimal> values = new LinkedHashMap<>(result.values());
        values.put(tag, PriceParser.normalizeComputed(value));
        saveTagValues(values);
    }

    public boolean removeTagValue(ResourceLocation tag) throws IOException {
        LoadResult<ResourceLocation, BigDecimal> result = loadTagValues();
        ensureWritable(result);
        Map<ResourceLocation, BigDecimal> values = new LinkedHashMap<>(result.values());
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
                    object.addProperty("value", PriceParser.toPlainString(entry.getValue()));
                    array.add(object);
                });
        JsonUtil.writeAtomicWithBackup(tagValuesPath, array);
    }

    private <K, V> void ensureWritable(LoadResult<K, V> result) throws ConfigWriteBlockedException {
        if (result.hasErrors()) {
            result.diagnostics().forEach(diagnostic -> ArcValueCalc.LOGGER.error("{}", diagnostic));
            throw new ConfigWriteBlockedException(result.diagnostics());
        }
    }

    private JsonArray readArray(Path path, List<ConfigDiagnostic> diagnostics) {
        try {
            JsonElement root = JsonUtil.read(path);
            if (!root.isJsonArray()) {
                addDiagnostic(diagnostics, path, "$", "root must be a JSON array");
                return null;
            }
            return root.getAsJsonArray();
        } catch (Exception e) {
            addDiagnostic(diagnostics, path, "$", e.getMessage());
            return null;
        }
    }

    private ValueKey readKey(JsonObject object, Path path, String location) {
        String item = requireString(object, "item", path, location);
        String nbt = object.has("nbt") ? normalizeNbt(requireString(object, "nbt", path, location + ".nbt"), path, location + ".nbt") : null;
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

    private BigDecimal readPrice(JsonObject object, String field, Path path, String location) {
        String value = requireString(object, field, path, location + "." + field);
        return PriceParser.parsePrice(value);
    }

    private JsonObject requireObject(JsonElement element, Path path, String location) {
        if (element == null || !element.isJsonObject()) {
            throw new IllegalArgumentException(path + " " + location + " must be an object");
        }
        return element.getAsJsonObject();
    }

    private String requireString(JsonObject object, String field, Path path, String location) {
        if (!object.has(field) || !object.get(field).isJsonPrimitive() || !object.get(field).getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException(path + " " + location + " requires string field '" + field + "'");
        }
        return object.get(field).getAsString();
    }

    private String normalizeNbt(String raw, Path path, String location) {
        try {
            return TagParser.parseTag(raw).toString();
        } catch (Exception e) {
            throw new IllegalArgumentException(path + " " + location + " invalid nbt: " + e.getMessage(), e);
        }
    }

    private void ensureSeedFile() {
        Path path = itemValuesPath;
        if (Files.exists(path)) {
            return;
        }
        JsonArray array = new JsonArray();
        addDefaultSeeds(array);
        try {
            JsonUtil.writeAtomicWithBackup(path, array);
        } catch (IOException e) {
            ArcValueCalc.LOGGER.error("Failed to create default item value file {}", path, e);
        }
    }

    private void ensureTagSeedFile() {
        Path path = tagValuesPath;
        if (Files.exists(path)) {
            return;
        }
        JsonArray array = new JsonArray();
        tagSeed(array, "minecraft:logs", "0.04");
        tagSeed(array, "minecraft:planks", "0.01");
        tagSeed(array, "forge:nuggets/iron", "0.01");
        tagSeed(array, "forge:nuggets/gold", "0.02");
        try {
            JsonUtil.writeAtomicWithBackup(path, array);
        } catch (IOException e) {
            ArcValueCalc.LOGGER.error("Failed to create default tag value file {}", path, e);
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

    private void addDiagnostic(List<ConfigDiagnostic> diagnostics, Path path, String location, String message) {
        ConfigDiagnostic diagnostic = new ConfigDiagnostic(path, location, message == null ? "unknown error" : message);
        diagnostics.add(diagnostic);
        ArcValueCalc.LOGGER.error("{}", diagnostic);
    }
}
