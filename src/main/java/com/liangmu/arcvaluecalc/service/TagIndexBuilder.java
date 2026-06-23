package com.liangmu.arcvaluecalc.service;

import com.liangmu.arcvaluecalc.model.ValueKey;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public final class TagIndexBuilder {
    public Map<ResourceLocation, Set<ValueKey>> build() {
        Map<ResourceLocation, Set<ValueKey>> index = new LinkedHashMap<>();
        for (Item item : ForgeRegistries.ITEMS) {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
            if (itemId == null) {
                continue;
            }
            item.builtInRegistryHolder().tags().forEach(tag -> add(index, tag, itemId));
        }
        return index;
    }

    private void add(Map<ResourceLocation, Set<ValueKey>> index, TagKey<Item> tag, ResourceLocation itemId) {
        if (!tag.isFor(Registries.ITEM)) {
            return;
        }
        index.computeIfAbsent(tag.location(), ignored -> new LinkedHashSet<>())
                .add(new ValueKey(itemId, (String) null));
    }
}
