package com.liangmu.arcvaluecalc.service;

import com.liangmu.arcvaluecalc.model.RuleIngredient;
import com.liangmu.arcvaluecalc.model.ValueRule;
import com.liangmu.arcvaluecalc.model.ValueSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.registries.ForgeRegistries;

final class AvaritiaRecipeAdapter {
    private static final String TIER_CRAFTING = "committee.nova.mods.avaritia.common.crafting.recipe.ITierCraftingRecipe";
    private static final String COMPRESSOR = "committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe";
    private static final String EXTREME_SMITHING = "committee.nova.mods.avaritia.common.crafting.recipe.ExtremeSmithingRecipe";
    private static final String NO_CONSUME_CATALYST = "committee.nova.mods.avaritia.common.crafting.recipe.NoConsumeCatalystShapedRecipe";
    private static final ResourceLocation INFINITY_CATALYST = new ResourceLocation("avaritia", "infinity_catalyst");

    Optional<ValueRule> fromRecipe(Recipe<?> recipe, RegistryAccess registryAccess) {
        try {
            if (isRecipeClass(recipe, COMPRESSOR)) {
                return compressorRule(recipe);
            }
            if (isRecipeClass(recipe, EXTREME_SMITHING)) {
                return extremeSmithingRule(recipe);
            }
            if (isRecipeClass(recipe, TIER_CRAFTING)) {
                return tableCraftingRule(recipe, registryAccess);
            }
            return Optional.empty();
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            throw new IllegalArgumentException("invalid Re-Avaritia recipe shape: " + recipe.getId(), e);
        }
    }

    private Optional<ValueRule> tableCraftingRule(Recipe<?> recipe, RegistryAccess registryAccess) {
        boolean skipInfinityCatalyst = isRecipeClass(recipe, NO_CONSUME_CATALYST);
        List<RuleIngredient> inputs = inputs(recipe.getIngredients(), 1, skipInfinityCatalyst);
        ItemStack result = recipe.getResultItem(registryAccess);
        if (inputs.isEmpty() || result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ValueRule(recipe.getId().toString(), inputs, List.of(RuleIngredient.fromStack(result.copy())), ValueSource.GENERATED_RULE));
    }

    private Optional<ValueRule> compressorRule(Recipe<?> recipe) throws ReflectiveOperationException {
        Object inputObject = invoke(recipe, "getInput");
        if (!(inputObject instanceof Ingredient ingredient)) {
            return Optional.empty();
        }
        Object countObject = invoke(recipe, "getInputCount");
        if (!(countObject instanceof Number number)) {
            return Optional.empty();
        }
        int count = number.intValue();
        RuleIngredient input = IngredientRuleConverter.fromIngredient(ingredient, count);
        Object resultObject = invoke(recipe, "getResultItem");
        if (input == null || !(resultObject instanceof ItemStack result) || result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ValueRule(recipe.getId().toString(), List.of(input), List.of(RuleIngredient.fromStack(result.copy())), ValueSource.GENERATED_RULE));
    }

    private Optional<ValueRule> extremeSmithingRule(Recipe<?> recipe) throws ReflectiveOperationException {
        List<RuleIngredient> inputs = new ArrayList<>();
        for (String fieldName : List.of("template", "base", "additions")) {
            Object fieldValue = field(recipe, fieldName);
            if (!(fieldValue instanceof Ingredient ingredient)) {
                return Optional.empty();
            }
            RuleIngredient input = IngredientRuleConverter.fromIngredient(ingredient);
            if (input == null) {
                return Optional.empty();
            }
            inputs.add(input);
        }
        Object resultObject = field(recipe, "result");
        if (!(resultObject instanceof ItemStack result) || result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ValueRule(recipe.getId().toString(), inputs, List.of(RuleIngredient.fromStack(result.copy())), ValueSource.GENERATED_RULE));
    }

    private List<RuleIngredient> inputs(List<Ingredient> ingredients, int count, boolean skipInfinityCatalyst) {
        List<RuleIngredient> inputs = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            if (ingredient.isEmpty()) {
                continue;
            }
            if (skipInfinityCatalyst && isInfinityCatalystOnly(ingredient)) {
                continue;
            }
            RuleIngredient input = IngredientRuleConverter.fromIngredient(ingredient, count);
            if (input == null) {
                return List.of();
            }
            inputs.add(input);
        }
        return inputs;
    }

    private boolean isInfinityCatalystOnly(Ingredient ingredient) {
        ItemStack[] stacks = ingredient.getItems();
        if (stacks.length == 0) {
            return false;
        }
        for (ItemStack stack : stacks) {
            if (!INFINITY_CATALYST.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))) {
                return false;
            }
        }
        return true;
    }

    private Object invoke(Object target, String methodName) throws ReflectiveOperationException {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }

    private Object field(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = target.getClass().getField(fieldName);
        return field.get(target);
    }

    private boolean isRecipeClass(Object recipe, String className) {
        Class<?> current = recipe.getClass();
        while (current != null) {
            if (className.equals(current.getName())) {
                return true;
            }
            for (Class<?> contract : current.getInterfaces()) {
                if (isInterface(contract, className)) {
                    return true;
                }
            }
            current = current.getSuperclass();
        }
        return false;
    }

    private boolean isInterface(Class<?> contract, String className) {
        if (className.equals(contract.getName())) {
            return true;
        }
        for (Class<?> parent : contract.getInterfaces()) {
            if (isInterface(parent, className)) {
                return true;
            }
        }
        return false;
    }
}
