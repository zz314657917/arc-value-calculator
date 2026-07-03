package com.liangmu.arcvaluecalc.service;

import com.liangmu.arcvaluecalc.model.RuleIngredient;
import com.liangmu.arcvaluecalc.model.ValueKey;
import com.liangmu.arcvaluecalc.model.ValueRule;
import com.liangmu.arcvaluecalc.model.ValueSource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

final class MekanismRecipeAdapter {
    private static final String ITEM_TO_ITEM = "mekanism.api.recipes.ItemStackToItemStackRecipe";
    private static final String ITEM_CHEMICAL_TO_ITEM = "mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe";
    private static final String COMBINER = "mekanism.api.recipes.CombinerRecipe";
    private static final String SAWMILL = "mekanism.api.recipes.SawmillRecipe";
    private static final String PRESSURIZED_REACTION = "mekanism.api.recipes.PressurizedReactionRecipe";

    Optional<ValueRule> fromRecipe(Recipe<?> recipe) {
        try {
            if (isRecipeClass(recipe, COMBINER)) {
                return itemRule(recipe, List.of(itemInput(recipe, "getMainInput"), itemInput(recipe, "getExtraInput")), outputs(recipe, "getOutputDefinition"));
            }
            if (isRecipeClass(recipe, SAWMILL)) {
                return itemRule(recipe, List.of(itemInput(recipe, "getInput")), outputs(recipe, "getMainOutputDefinition"));
            }
            if (isRecipeClass(recipe, PRESSURIZED_REACTION)) {
                return itemRule(recipe, List.of(itemInput(recipe, "getInputSolid")), outputs(recipe, "getOutputDefinition"));
            }
            if (isRecipeClass(recipe, ITEM_CHEMICAL_TO_ITEM)) {
                return itemRule(recipe, List.of(itemInput(recipe, "getItemInput")), outputs(recipe, "getOutputDefinition"));
            }
            if (isRecipeClass(recipe, ITEM_TO_ITEM)) {
                return itemRule(recipe, List.of(itemInput(recipe, "getInput")), outputs(recipe, "getOutputDefinition"));
            }
            return Optional.empty();
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            throw new IllegalArgumentException("invalid Mekanism recipe shape: " + recipe.getId(), e);
        }
    }

    private Optional<ValueRule> itemRule(Recipe<?> recipe, List<RuleIngredient> inputs, List<RuleIngredient> outputs) {
        if (inputs.isEmpty() || inputs.stream().anyMatch(input -> input == null) || outputs.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(new ValueRule(recipe.getId().toString(), inputs, outputs, ValueSource.GENERATED_RULE));
    }

    private RuleIngredient itemInput(Object recipe, String methodName) throws ReflectiveOperationException {
        Object ingredient = invoke(recipe, methodName);
        if (ingredient == null) {
            return null;
        }
        Object representations = invoke(ingredient, "getRepresentations");
        if (!(representations instanceof List<?> stacks) || stacks.isEmpty()) {
            return null;
        }
        Set<ValueKey> choices = new LinkedHashSet<>();
        Integer requiredCount = null;
        for (Object representation : stacks) {
            if (!(representation instanceof ItemStack stack) || stack.isEmpty()) {
                continue;
            }
            ItemStack copy = stack.copy();
            int count = neededAmount(ingredient, copy);
            if (count <= 0 || count > RuleIngredient.MAX_COUNT) {
                return null;
            }
            if (requiredCount == null) {
                requiredCount = count;
            } else if (requiredCount != count) {
                return null;
            }
            copy.setCount(count);
            choices.add(RuleIngredient.fromStack(copy).asKey());
        }
        if (choices.isEmpty() || requiredCount == null) {
            return null;
        }
        if (choices.size() == 1) {
            ValueKey key = choices.iterator().next();
            return RuleIngredient.item(key, requiredCount);
        }
        return RuleIngredient.choices(List.copyOf(choices), requiredCount);
    }

    private int neededAmount(Object ingredient, ItemStack stack) {
        for (Method method : ingredient.getClass().getMethods()) {
            if (!"getNeededAmount".equals(method.getName()) || method.getParameterCount() != 1) {
                continue;
            }
            try {
                Object value = method.invoke(ingredient, stack);
                if (value instanceof Number number) {
                    long amount = number.longValue();
                    if (amount > Integer.MAX_VALUE) {
                        return -1;
                    }
                    return (int) amount;
                }
            } catch (ReflectiveOperationException ignored) {
                // Fall back to the representation count below.
            }
        }
        return stack.getCount();
    }

    private List<RuleIngredient> outputs(Object recipe, String methodName) throws ReflectiveOperationException {
        Object outputDefinition = invoke(recipe, methodName);
        if (!(outputDefinition instanceof List<?> definitions) || definitions.isEmpty()) {
            return List.of();
        }
        List<RuleIngredient> outputs = new ArrayList<>();
        for (Object definition : definitions) {
            ItemStack stack = asItemStack(definition);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            outputs.add(RuleIngredient.fromStack(stack.copy()));
        }
        return outputs;
    }

    private ItemStack asItemStack(Object definition) throws ReflectiveOperationException {
        if (definition instanceof ItemStack stack) {
            return stack;
        }
        Object item = tryInvoke(definition, "item");
        if (item instanceof ItemStack stack) {
            return stack;
        }
        Object itemOutput = tryInvoke(definition, "getItemOutput");
        if (itemOutput instanceof ItemStack stack) {
            return stack;
        }
        return null;
    }

    private Object invoke(Object target, String methodName) throws ReflectiveOperationException {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }

    private Object tryInvoke(Object target, String methodName) throws ReflectiveOperationException {
        try {
            return invoke(target, methodName);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private boolean isRecipeClass(Object recipe, String className) {
        Class<?> current = recipe.getClass();
        while (current != null) {
            if (className.equals(current.getName())) {
                return true;
            }
            current = current.getSuperclass();
        }
        return false;
    }
}
