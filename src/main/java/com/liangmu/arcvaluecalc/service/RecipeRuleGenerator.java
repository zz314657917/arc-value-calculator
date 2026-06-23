package com.liangmu.arcvaluecalc.service;

import com.liangmu.arcvaluecalc.model.RuleIngredient;
import com.liangmu.arcvaluecalc.model.ValueRule;
import com.liangmu.arcvaluecalc.model.ValueSource;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraftforge.registries.ForgeRegistries;

public final class RecipeRuleGenerator {
    public List<ValueRule> generate(RecipeManager recipeManager, RegistryAccess registryAccess) {
        List<ValueRule> rules = new ArrayList<>();
        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            ValueRule rule = fromRecipe(recipe, registryAccess);
            if (rule != null) {
                rules.add(rule);
            }
        }
        return rules;
    }

    private ValueRule fromRecipe(Recipe<?> recipe, RegistryAccess registryAccess) {
        if (!(recipe instanceof ShapedRecipe
                || recipe instanceof ShapelessRecipe
                || recipe instanceof AbstractCookingRecipe
                || recipe instanceof StonecutterRecipe
                || recipe instanceof SmithingTransformRecipe)) {
            return null;
        }
        List<RuleIngredient> inputs = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            RuleIngredient input = fromIngredient(ingredient);
            if (input == null) {
                return null;
            }
            inputs.add(input);
        }
        ItemStack result = recipe.getResultItem(registryAccess);
        if (result.isEmpty()) {
            return null;
        }
        RuleIngredient output = RuleIngredient.fromStack(result);
        ResourceLocation id = recipe.getId();
        return new ValueRule(id == null ? "unknown" : id.toString(), inputs, List.of(output), ValueSource.GENERATED_RULE);
    }

    private RuleIngredient fromIngredient(Ingredient ingredient) {
        if (ingredient.isEmpty()) {
            return null;
        }
        ItemStack[] items = ingredient.getItems();
        if (items.length == 0) {
            return null;
        }
        if (items.length == 1) {
            ItemStack stack = items[0].copy();
            stack.setCount(1);
            return RuleIngredient.fromStack(stack);
        }
        // Multi-item ingredients are represented as the first available item in v1 generated rules.
        // Manual rules can use tags when a pack wants explicit ore/tag semantics.
        ItemStack stack = items[0].copy();
        stack.setCount(1);
        if (ForgeRegistries.ITEMS.getKey(stack.getItem()) == null) {
            return null;
        }
        return RuleIngredient.fromStack(stack);
    }
}
