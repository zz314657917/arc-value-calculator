package com.liangmu.arcvaluecalc;

import com.liangmu.arcvaluecalc.service.ValueServices;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public final class RecipeReloadListener implements PreparableReloadListener {
    @Override
    public @NotNull CompletableFuture<Void> reload(
            PreparationBarrier barrier,
            ResourceManager resourceManager,
            net.minecraft.util.profiling.ProfilerFiller preparationsProfiler,
            net.minecraft.util.profiling.ProfilerFiller reloadProfiler,
            Executor backgroundExecutor,
            Executor gameExecutor
    ) {
        return CompletableFuture.supplyAsync(() -> Unit.INSTANCE, backgroundExecutor)
                .thenCompose(barrier::wait)
                .thenRunAsync(() -> {
                    if (ServerLifecycleHooks.getCurrentServer() != null) {
                        RecipeManager recipeManager = ServerLifecycleHooks.getCurrentServer().getRecipeManager();
                        ValueServices.server().reload(
                                recipeManager,
                                ServerLifecycleHooks.getCurrentServer().registryAccess(),
                                true
                        );
                    }
                }, gameExecutor);
    }
}
