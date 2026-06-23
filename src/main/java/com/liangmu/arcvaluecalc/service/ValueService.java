package com.liangmu.arcvaluecalc.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.liangmu.arcvaluecalc.ArcValueCalc;
import com.liangmu.arcvaluecalc.config.ArcValueConfig;
import com.liangmu.arcvaluecalc.model.MatchType;
import com.liangmu.arcvaluecalc.model.ValueEntry;
import com.liangmu.arcvaluecalc.model.ValueKey;
import com.liangmu.arcvaluecalc.model.ValueLookupResult;
import com.liangmu.arcvaluecalc.model.ValueRule;
import com.liangmu.arcvaluecalc.model.ValueSource;
import com.liangmu.arcvaluecalc.network.ArcValueNetwork;
import com.liangmu.arcvaluecalc.storage.ConfigDiagnostic;
import com.liangmu.arcvaluecalc.storage.ConfigWriteBlockedException;
import com.liangmu.arcvaluecalc.storage.JsonUtil;
import com.liangmu.arcvaluecalc.storage.LoadResult;
import com.liangmu.arcvaluecalc.storage.RuleFileStore;
import com.liangmu.arcvaluecalc.storage.ValueFileStore;
import com.liangmu.arcvaluecalc.storage.ValuePaths;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.server.ServerLifecycleHooks;

public final class ValueService {
    private final boolean writeGeneratedRuleFiles;
    private final ValueFileStore valueFileStore = new ValueFileStore();
    private final RuleFileStore ruleFileStore = new RuleFileStore();
    private final RecipeRuleGenerator recipeRuleGenerator = new RecipeRuleGenerator();
    private final TagIndexBuilder tagIndexBuilder = new TagIndexBuilder();
    private final ValueCalculator calculator = new ValueCalculator();
    private Map<ValueKey, ValueEntry> values = new LinkedHashMap<>();
    private Map<ValueKey, BigDecimal> manualValues = new LinkedHashMap<>();
    private List<ConfigDiagnostic> configDiagnostics = List.of();
    private long generation;

    ValueService(boolean writeGeneratedRuleFiles) {
        this.writeGeneratedRuleFiles = writeGeneratedRuleFiles;
    }

    public synchronized void reload(RecipeManager recipeManager, RegistryAccess registryAccess, boolean notifyClients) {
        LoadResult<ValueKey, BigDecimal> manualResult = valueFileStore.loadManualValues();
        LoadResult<ResourceLocation, BigDecimal> tagResult = valueFileStore.loadTagValues();
        List<ConfigDiagnostic> diagnostics = new ArrayList<>();
        diagnostics.addAll(manualResult.diagnostics());
        diagnostics.addAll(tagResult.diagnostics());
        configDiagnostics = List.copyOf(diagnostics);
        manualValues = new LinkedHashMap<>(manualResult.values());
        Map<ResourceLocation, Set<ValueKey>> tagIndex = tagIndexBuilder.build();
        expandTagValues(manualValues, tagResult.values(), tagIndex);
        List<ValueRule> manualRules = ruleFileStore.loadManualRules();
        List<ValueRule> generatedRules = recipeManager == null ? ruleFileStore.loadGeneratedRules() : recipeRuleGenerator.generate(recipeManager, registryAccess);
        if (writeGeneratedRuleFiles && recipeManager != null && ArcValueConfig.GENERATE_RULE_FILES.get()) {
            try {
                ruleFileStore.writeGeneratedRules(generatedRules);
            } catch (IOException e) {
                ArcValueCalc.LOGGER.error("Failed to write generated value rules", e);
            }
        }
        values = calculator.calculate(
                manualValues,
                manualRules,
                generatedRules,
                tagIndex,
                ArcValueConfig.MAX_ITERATIONS.get()
        ).values();
        generation++;
        if (notifyClients) {
            ArcValueNetwork.sendReload(generation);
        }
    }

    public synchronized Optional<BigDecimal> getValue(ItemStack stack) {
        ValueEntry exact = values.get(ValueKey.exact(stack));
        if (exact != null) {
            return Optional.of(exact.value());
        }
        ValueEntry itemOnly = values.get(ValueKey.itemOnly(stack));
        return itemOnly == null ? Optional.empty() : Optional.of(itemOnly.value());
    }

    public synchronized ValueLookupResult lookup(ItemStack stack) {
        ValueKey exactKey = ValueKey.exact(stack);
        ValueEntry exact = values.get(exactKey);
        if (exact != null) {
            return new ValueLookupResult(MatchType.EXACT, exactKey, exact.value(), generation);
        }
        ValueKey itemOnlyKey = ValueKey.itemOnly(stack);
        ValueEntry itemOnly = values.get(itemOnlyKey);
        if (itemOnly != null) {
            return new ValueLookupResult(MatchType.ITEM_ONLY, itemOnlyKey, itemOnly.value(), generation);
        }
        return ValueLookupResult.missing(exactKey, generation);
    }

    public synchronized ValueSource getSource(ItemStack stack) {
        ValueEntry exact = values.get(ValueKey.exact(stack));
        if (exact != null) {
            return exact.source();
        }
        ValueEntry itemOnly = values.get(ValueKey.itemOnly(stack));
        return itemOnly == null ? ValueSource.NONE : itemOnly.source();
    }

    public synchronized Map<ValueKey, BigDecimal> snapshot() {
        Map<ValueKey, BigDecimal> snapshot = new LinkedHashMap<>();
        values.forEach((key, entry) -> snapshot.put(key, entry.value()));
        return snapshot;
    }

    public synchronized int size() {
        return values.size();
    }

    public synchronized void setManualValue(ValueKey key, BigDecimal value, RecipeManager recipeManager) throws IOException {
        valueFileStore.setManualValue(key, value);
        reload(recipeManager, currentRegistryAccess(), true);
    }

    public synchronized boolean removeManualValue(ValueKey key, RecipeManager recipeManager) throws IOException {
        boolean removed = valueFileStore.removeManualValue(key);
        reload(recipeManager, currentRegistryAccess(), true);
        return removed;
    }

    public synchronized void setTagValue(ResourceLocation tag, BigDecimal value, RecipeManager recipeManager) throws IOException {
        valueFileStore.setTagValue(tag, value);
        reload(recipeManager, currentRegistryAccess(), true);
    }

    public synchronized boolean removeTagValue(ResourceLocation tag, RecipeManager recipeManager) throws IOException {
        boolean removed = valueFileStore.removeTagValue(tag);
        reload(recipeManager, currentRegistryAccess(), true);
        return removed;
    }

    public synchronized Path exportValues() throws IOException {
        refreshConfigDiagnostics();
        ensureNoConfigErrors();
        Files.createDirectories(ValuePaths.exportDir());
        Path path = ValuePaths.exportDir().resolve("values_export.json");
        JsonArray array = new JsonArray();
        values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey((a, b) -> a.toString().compareTo(b.toString())))
                .forEach(entry -> {
                    JsonObject object = new JsonObject();
                    object.addProperty("item", entry.getKey().item().toString());
                    if (entry.getKey().nbt() != null) {
                        object.addProperty("nbt", entry.getKey().nbt());
                    }
                    object.addProperty("value", PriceParser.toPlainString(entry.getValue().value()));
                    object.addProperty("source", entry.getValue().source().name());
                    array.add(object);
                });
        JsonUtil.write(path, array);
        return path;
    }

    public Path exportRules() {
        return ValuePaths.generatedRules();
    }

    public synchronized long generation() {
        return generation;
    }

    public synchronized List<ConfigDiagnostic> configDiagnostics() {
        return configDiagnostics;
    }

    private void ensureNoConfigErrors() throws ConfigWriteBlockedException {
        if (!configDiagnostics.isEmpty()) {
            throw new ConfigWriteBlockedException(configDiagnostics);
        }
    }

    private void refreshConfigDiagnostics() {
        LoadResult<ValueKey, BigDecimal> manualResult = valueFileStore.loadManualValues();
        LoadResult<ResourceLocation, BigDecimal> tagResult = valueFileStore.loadTagValues();
        List<ConfigDiagnostic> diagnostics = new ArrayList<>();
        diagnostics.addAll(manualResult.diagnostics());
        diagnostics.addAll(tagResult.diagnostics());
        configDiagnostics = List.copyOf(diagnostics);
    }

    private RegistryAccess currentRegistryAccess() {
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            return ServerLifecycleHooks.getCurrentServer().registryAccess();
        }
        return RegistryAccess.EMPTY;
    }

    private void expandTagValues(
            Map<ValueKey, BigDecimal> itemValues,
            Map<ResourceLocation, BigDecimal> tagValues,
            Map<ResourceLocation, Set<ValueKey>> tagIndex
    ) {
        tagValues.forEach((tag, value) -> {
            Set<ValueKey> keys = tagIndex.get(tag);
            if (keys == null) {
                return;
            }
            for (ValueKey key : keys) {
                itemValues.merge(key, value, (oldValue, newValue) -> newValue.compareTo(oldValue) < 0 ? newValue : oldValue);
            }
        });
    }
}
