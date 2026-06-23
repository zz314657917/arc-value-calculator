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
        json.getAsJsonArray("input").forEach(element -> inputs.add(RuleIngredient.fromJson(element.getAsJsonObject())));
        List<RuleIngredient> outputs = new ArrayList<>();
        json.getAsJsonArray("output").forEach(element -> outputs.add(RuleIngredient.fromJson(element.getAsJsonObject())));
        return new ValueRule(id, inputs, outputs, source);
    }
}
