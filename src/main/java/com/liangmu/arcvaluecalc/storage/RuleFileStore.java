package com.liangmu.arcvaluecalc.storage;

import com.google.gson.JsonElement;
import com.liangmu.arcvaluecalc.ArcValueCalc;
import com.liangmu.arcvaluecalc.model.ValueRule;
import com.liangmu.arcvaluecalc.model.ValueSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class RuleFileStore {
    private final Path manualRulesPath;
    private final Path generatedRulesPath;

    public RuleFileStore() {
        this(ValuePaths.manualRules(), ValuePaths.generatedRules());
    }

    public RuleFileStore(Path manualRulesPath, Path generatedRulesPath) {
        this.manualRulesPath = manualRulesPath;
        this.generatedRulesPath = generatedRulesPath;
    }

    public List<ValueRule> loadManualRules() {
        return loadRules(manualRulesPath, ValueSource.MANUAL_RULE);
    }

    public List<ValueRule> loadGeneratedRules() {
        return loadRules(generatedRulesPath, ValueSource.GENERATED_RULE);
    }

    public void writeGeneratedRules(List<ValueRule> rules) throws IOException {
        Path root = generatedRulesPath.toAbsolutePath().normalize();
        Path parent = root.getParent();
        if (parent == null) {
            throw new IOException("Generated rule root has no parent: " + root);
        }
        Files.createDirectories(parent);
        Path tempRoot = Files.createTempDirectory(parent, root.getFileName() + ".tmp.");
        try {
            writeGeneratedRulesTo(rules, tempRoot);
            replaceDirectory(tempRoot, root);
        } catch (IOException | RuntimeException e) {
            deleteTree(tempRoot);
            throw e;
        }
    }

    private void writeGeneratedRulesTo(List<ValueRule> rules, Path root) throws IOException {
        Files.createDirectories(root);
        Map<Path, String> occupiedPaths = new HashMap<>();
        for (ValueRule rule : rules) {
            String id = sanitize(rule.id());
            validateRelativeRulePath(id);
            Path path = root.resolve(id + ".json").normalize();
            if (!path.startsWith(root)) {
                throw new IOException("Generated rule path escapes root: " + id);
            }
            String previousId = occupiedPaths.putIfAbsent(path, rule.id());
            if (previousId != null) {
                throw new IOException("Generated rule path collision: " + previousId + " and " + rule.id() + " -> " + path);
            }
            JsonUtil.write(path, rule.toJson());
        }
    }

    private void replaceDirectory(Path tempRoot, Path root) throws IOException {
        Path oldRoot = null;
        if (Files.exists(root)) {
            oldRoot = Files.createTempDirectory(root.getParent(), root.getFileName() + ".old.");
            moveDirectory(root, oldRoot);
        }
        try {
            moveDirectory(tempRoot, root);
            if (oldRoot != null) {
                try {
                    deleteTree(oldRoot);
                } catch (IOException cleanupError) {
                    ArcValueCalc.LOGGER.warn("Failed to clean old generated rule directory {}", oldRoot, cleanupError);
                }
            }
        } catch (IOException e) {
            if (oldRoot != null && !Files.exists(root)) {
                try {
                    moveDirectory(oldRoot, root);
                } catch (IOException restoreError) {
                    e.addSuppressed(restoreError);
                }
            }
            throw e;
        }
    }

    private void moveDirectory(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicError) {
            try {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException fallbackError) {
                fallbackError.addSuppressed(atomicError);
                throw fallbackError;
            }
        }
    }

    private void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(root)) {
            List<Path> paths = stream.sorted(Comparator.reverseOrder()).toList();
            for (Path path : paths) {
                Files.deleteIfExists(path);
            }
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
                } catch (Exception e) {
                    ArcValueCalc.LOGGER.error("Failed to load value rule {}", path, e);
                }
            }
        } catch (IOException e) {
            ArcValueCalc.LOGGER.error("Failed to scan value rule directory {}", root, e);
        }
        return rules;
    }

    String sanitize(String id) {
        return id.replace(':', '/').replaceAll("[^a-zA-Z0-9_.-/-]", "_");
    }

    private void validateRelativeRulePath(String id) throws IOException {
        if (id == null || id.isBlank()) {
            throw new IOException("Generated rule id is blank");
        }
        String[] segments = id.split("/");
        for (String segment : segments) {
            if (segment.isBlank() || ".".equals(segment) || "..".equals(segment)) {
                throw new IOException("Generated rule id contains invalid path segment: " + id);
            }
        }
    }
}
