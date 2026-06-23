package com.liangmu.arcvaluecalc.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public final class ValueRule {
    private final List<RuleIngredient> inputs;
    private final List<RuleIngredient> outputs;
    private final ValueSource source;
    private final String id;

    public ValueRule(String id, List<RuleIngredient> inputs, List<RuleIngredient> outputs, ValueSource source) {
        if (inputs == null || inputs.isEmpty()) {
            throw new IllegalArgumentException("rule input must not be empty: " + id);
        }
        if (outputs == null || outputs.isEmpty()) {
            throw new IllegalArgumentException("rule output must not be empty: " + id);
        }
        for (RuleIngredient output : outputs) {
            if (output.asKey() == null) {
                throw new IllegalArgumentException("rule output must be an item: " + id);
            }
        }
        this.id = id;
        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.source = source;
    }

    public String id() {
        return id;
    }

    public List<RuleIngredient> inputs() {
        return inputs;
    }

    public List<RuleIngredient> outputs() {
        return outputs;
    }

    public ValueSource source() {
        return source;
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        JsonArray input = new JsonArray();
        for (RuleIngredient ingredient : inputs) {
            input.add(ingredient.toJson());
        }
        JsonArray output = new JsonArray();
        for (RuleIngredient ingredient : outputs) {
            output.add(ingredient.toJson());
        }
        root.add("input", input);
        root.add("output", output);
        return root;
    }

    public static ValueRule fromJson(String id, JsonObject json, ValueSource source) {
        List<RuleIngredient> inputs = new ArrayList<>();
        JsonArray inputArray = json.has("input") ? json.getAsJsonArray("input") : json.getAsJsonArray("inputs");
        if (inputArray == null) {
            throw new IllegalArgumentException("rule requires input array: " + id);
        }
        for (int i = 0; i < inputArray.size(); i++) {
            try {
                inputs.add(RuleIngredient.fromJson(inputArray.get(i).getAsJsonObject()));
            } catch (Exception e) {
                throw new IllegalArgumentException("input[" + i + "] " + e.getMessage(), e);
            }
        }
        List<RuleIngredient> outputs = new ArrayList<>();
        JsonArray outputArray = json.has("output") ? json.getAsJsonArray("output") : json.getAsJsonArray("outputs");
        if (outputArray == null) {
            throw new IllegalArgumentException("rule requires output array: " + id);
        }
        for (int i = 0; i < outputArray.size(); i++) {
            try {
                outputs.add(RuleIngredient.fromJson(outputArray.get(i).getAsJsonObject()));
            } catch (Exception e) {
                throw new IllegalArgumentException("output[" + i + "] " + e.getMessage(), e);
            }
        }
        return new ValueRule(id, inputs, outputs, source);
    }
}
