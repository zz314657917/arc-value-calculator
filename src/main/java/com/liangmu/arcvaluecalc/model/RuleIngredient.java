package com.liangmu.arcvaluecalc.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class RuleIngredient {
    public static final int MAX_COUNT = 1_000_000;

    private final ResourceLocation item;
    private final ResourceLocation tag;
    private final CompoundTag nbt;
    private final List<ValueKey> choices;
    private final int count;

    private RuleIngredient(ResourceLocation item, ResourceLocation tag, CompoundTag nbt, List<ValueKey> choices, int count) {
        if (count <= 0 || count > MAX_COUNT) {
            throw new IllegalArgumentException("count must be between 1 and " + MAX_COUNT);
        }
        this.item = item;
        this.tag = tag;
        this.nbt = nbt;
        this.choices = choices == null ? List.of() : List.copyOf(choices);
        this.count = count;
        int modes = (item == null ? 0 : 1) + (tag == null ? 0 : 1) + (this.choices.isEmpty() ? 0 : 1);
        if (modes != 1) {
            throw new IllegalArgumentException("ingredient requires exactly one of item, tag, or choices");
        }
    }

    public static RuleIngredient item(ResourceLocation item, int count) {
        return new RuleIngredient(Objects.requireNonNull(item), null, null, null, count);
    }

    public static RuleIngredient item(ResourceLocation item, CompoundTag nbt, int count) {
        return new RuleIngredient(Objects.requireNonNull(item), null, nbt, null, count);
    }

    public static RuleIngredient tag(ResourceLocation tag, int count) {
        return new RuleIngredient(null, Objects.requireNonNull(tag), null, null, count);
    }

    public static RuleIngredient choices(List<ValueKey> choices, int count) {
        if (choices == null || choices.isEmpty()) {
            throw new IllegalArgumentException("choices must not be empty");
        }
        return new RuleIngredient(null, null, null, choices, count);
    }

    public ResourceLocation item() {
        return item;
    }

    public ResourceLocation tag() {
        return tag;
    }

    public CompoundTag nbt() {
        return nbt;
    }

    public List<ValueKey> choices() {
        return choices;
    }

    public int count() {
        return count;
    }

    public boolean isTag() {
        return tag != null;
    }

    public boolean isChoices() {
        return !choices.isEmpty();
    }

    public ValueKey asKey() {
        if (item == null) {
            return null;
        }
        return new ValueKey(item, nbt);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (item != null) {
            json.addProperty("item", item.toString());
        }
        if (tag != null) {
            json.addProperty("tag", tag.toString());
        }
        if (!choices.isEmpty()) {
            JsonArray array = new JsonArray();
            for (ValueKey choice : choices) {
                JsonObject choiceJson = new JsonObject();
                choiceJson.addProperty("item", choice.item().toString());
                if (choice.nbt() != null) {
                    choiceJson.addProperty("nbt", choice.nbt());
                }
                array.add(choiceJson);
            }
            json.add("choices", array);
        }
        if (nbt != null && !nbt.isEmpty()) {
            json.addProperty("nbt", nbt.toString());
        }
        if (count != 1) {
            json.addProperty("count", count);
        }
        return json;
    }

    public static RuleIngredient fromJson(JsonObject json) {
        int count = json.has("count") ? json.get("count").getAsInt() : 1;
        int modes = (json.has("item") ? 1 : 0) + (json.has("tag") ? 1 : 0) + (json.has("choices") ? 1 : 0);
        if (modes != 1) {
            throw new IllegalArgumentException("ingredient requires exactly one of item, tag, or choices");
        }
        CompoundTag nbt = null;
        if (json.has("nbt")) {
            try {
                nbt = TagParser.parseTag(json.get("nbt").getAsString());
            } catch (Exception e) {
                throw new IllegalArgumentException("invalid nbt: " + e.getMessage(), e);
            }
        }
        if (json.has("item")) {
            return item(new ResourceLocation(json.get("item").getAsString()), nbt, count);
        }
        if (json.has("tag")) {
            if (nbt != null) {
                throw new IllegalArgumentException("tag ingredient must not define nbt");
            }
            return tag(new ResourceLocation(json.get("tag").getAsString()), count);
        }
        if (json.has("choices")) {
            if (nbt != null) {
                throw new IllegalArgumentException("choices ingredient must put nbt on each choice");
            }
            return choices(readChoices(json.get("choices")), count);
        }
        throw new IllegalArgumentException("ingredient requires item, tag, or choices: " + json);
    }

    private static List<ValueKey> readChoices(JsonElement element) {
        if (element == null || !element.isJsonArray()) {
            throw new IllegalArgumentException("choices must be an array");
        }
        JsonArray array = element.getAsJsonArray();
        if (array.isEmpty()) {
            throw new IllegalArgumentException("choices must not be empty");
        }
        List<ValueKey> keys = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            String item = object.get("item").getAsString();
            String nbt = null;
            if (object.has("nbt")) {
                nbt = object.get("nbt").getAsString();
                try {
                    TagParser.parseTag(nbt);
                } catch (Exception e) {
                    throw new IllegalArgumentException("choices[" + i + "] invalid nbt: " + e.getMessage(), e);
                }
            }
            keys.add(new ValueKey(new ResourceLocation(item), nbt));
        }
        return keys;
    }

    public static RuleIngredient fromStack(ItemStack stack) {
        ResourceLocation id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) {
            throw new IllegalArgumentException("Unregistered item: " + stack);
        }
        return item(id, stack.getTag(), stack.getCount());
    }

    @Override
    public String toString() {
        return toJson().toString();
    }
}
