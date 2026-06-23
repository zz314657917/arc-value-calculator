package com.liangmu.arcvaluecalc.storage;

import com.google.gson.JsonElement;
import com.liangmu.arcvaluecalc.model.ValueRule;
import com.liangmu.arcvaluecalc.model.ValueSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class RuleFileStore {
    public List<ValueRule> loadManualRules() {
        return loadRules(ValuePaths.manualRules(), ValueSource.MANUAL_RULE);
    }

    public List<ValueRule> loadGeneratedRules() {
        return loadRules(ValuePaths.generatedRules(), ValueSource.GENERATED_RULE);
    }

    public void writeGeneratedRules(List<ValueRule> rules) throws IOException {
        Path root = ValuePaths.generatedRules();
        if (Files.exists(root)) {
            try (Stream<Path> stream = Files.walk(root)) {
                List<Path> paths = stream.sorted(Comparator.reverseOrder()).toList();
                for (Path path : paths) {
                    if (!path.equals(root)) {
                        Files.deleteIfExists(path);
                    }
                }
            }
        }
        Files.createDirectories(root);
        for (ValueRule rule : rules) {
            String id = sanitize(rule.id());
            Path path = root.resolve(id + ".json");
            JsonUtil.write(path, rule.toJson());
        }
    }

    private List<ValueRule> loadRules(Path root, ValueSource source) {
        List<ValueRule> rules = new ArrayList<>();
        if (!Files.exists(root)) {
            return rules;
        }
        try (Stream<Path> stream = Files.walk(root)) {
            List<Path> paths = stream.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".json"))
                    .sorted()
                    .toList();
            for (Path path : paths) {
                try {
                    JsonElement element = JsonUtil.read(path);
                    String id = root.relativize(path).toString().replace('\\', '/').replaceAll("\\.json$", "");
                    rules.add(ValueRule.fromJson(id, element.getAsJsonObject(), source));
                } catch (Exception ignored) {
                }
            }
        } catch (IOException ignored) {
        }
        return rules;
    }

    private String sanitize(String id) {
        return id.replace(':', '/').replaceAll("[^a-zA-Z0-9_./-]", "_");
    }
}
