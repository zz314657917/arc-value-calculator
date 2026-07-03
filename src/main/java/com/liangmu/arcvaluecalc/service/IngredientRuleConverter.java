package com.liangmu.arcvaluecalc.service;

import com.liangmu.arcvaluecalc.model.RuleIngredient;
import com.liangmu.arcvaluecalc.model.ValueKey;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

final class IngredientRuleConverter {
    private IngredientRuleConverter() {
    }

    static RuleIngredient fromIngredient(Ingredient ingredient) {
        return fromIngredient(ingredient, 1);
    }

    static RuleIngredient fromIngredient(Ingredient ingredient, int count) {
        if (ingredient == null || ingredient.isEmpty() || count <= 0 || count > RuleIngredient.MAX_COUNT) {
            return null;
        }
        ItemStack[] items = ingredient.getItems();
        if (items.length == 0) {
            return null;
        }
        if (items.length == 1) {
            ItemStack stack = items[0].copy();
            stack.setCount(count);
            return RuleIngredient.fromStack(stack);
        }
        Set<ValueKey> choices = new LinkedHashSet<>();
        for (ItemStack item : items) {
            ItemStack stack = item.copy();
            stack.setCount(1);
            if (ForgeRegistries.ITEMS.getKey(stack.getItem()) != null) {
                choices.add(RuleIngredient.fromStack(stack).asKey());
            }
        }
        if (choices.isEmpty()) {
            return null;
        }
        return RuleIngredient.choices(List.copyOf(choices), count);
    }
}
