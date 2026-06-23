package com.liangmu.arcvaluecalc.model;

import com.google.gson.JsonObject;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class RuleIngredient {
    private final ResourceLocation item;
    private final ResourceLocation tag;
    private final CompoundTag nbt;
    private final int count;

    private RuleIngredient(ResourceLocation item, ResourceLocation tag, CompoundTag nbt, int count) {
        this.item = item;
        this.tag = tag;
        this.nbt = nbt;
        this.count = Math.max(1, count);
    }

    public static RuleIngredient item(ResourceLocation item, int count) {
        return new RuleIngredient(Objects.requireNonNull(item), null, null, count);
    }

    public static RuleIngredient item(ResourceLocation item, CompoundTag nbt, int count) {
        return new RuleIngredient(Objects.requireNonNull(item), null, nbt, count);
    }

    public static RuleIngredient tag(ResourceLocation tag, int count) {
        return new RuleIngredient(null, Objects.requireNonNull(tag), null, count);
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

    public int count() {
        return count;
    }

    public boolean isTag() {
        return tag != null;
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
        CompoundTag nbt = null;
        if (json.has("nbt")) {
            try {
                nbt = TagParser.parseTag(json.get("nbt").getAsString());
            } catch (Exception ignored) {
                nbt = null;
            }
        }
        if (json.has("item")) {
            return item(new ResourceLocation(json.get("item").getAsString()), nbt, count);
        }
        if (json.has("tag")) {
            return tag(new ResourceLocation(json.get("tag").getAsString()), count);
        }
        throw new IllegalArgumentException("Rule ingredient requires item or tag: " + json);
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
