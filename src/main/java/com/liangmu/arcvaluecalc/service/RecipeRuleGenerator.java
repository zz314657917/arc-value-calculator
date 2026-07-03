package com.liangmu.arcvaluecalc.service;

import com.liangmu.arcvaluecalc.ArcValueCalc;
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

public final class RecipeRuleGenerator {
    private final AvaritiaRecipeAdapter avaritiaRecipeAdapter = new AvaritiaRecipeAdapter();
    private final MekanismRecipeAdapter mekanismRecipeAdapter = new MekanismRecipeAdapter();

    public List<ValueRule> generate(RecipeManager recipeManager, RegistryAccess registryAccess) {
        List<ValueRule> rules = new ArrayList<>();
        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            try {
                ValueRule rule = fromRecipe(recipe, registryAccess);
                if (rule != null) {
                    rules.add(rule);
                }
            } catch (Exception e) {
                ArcValueCalc.LOGGER.warn("Skipping recipe {} during value rule generation", recipe.getId(), e);
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
            return avaritiaRecipeAdapter.fromRecipe(recipe, registryAccess)
                    .or(() -> mekanismRecipeAdapter.fromRecipe(recipe))
                    .orElse(null);
        }
        List<RuleIngredient> inputs = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) {
                continue;
            }
            RuleIngredient input = IngredientRuleConverter.fromIngredient(ingredient);
            if (input == null) {
                return null;
            }
            inputs.add(input);
        }
        if (inputs.isEmpty()) {
            return null;
        }
        ItemStack result = recipe.getResultItem(registryAccess);
        if (result.isEmpty()) {
            return null;
        }
        RuleIngredient output = RuleIngredient.fromStack(result);
        ResourceLocation id = recipe.getId();
        return new ValueRule(id == null ? "unknown" : id.toString(), inputs, List.of(output), ValueSource.GENERATED_RULE);
    }
}
