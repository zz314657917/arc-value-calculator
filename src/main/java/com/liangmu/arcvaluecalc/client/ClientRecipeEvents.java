package com.liangmu.arcvaluecalc.client;

import com.liangmu.arcvaluecalc.ArcValueCalc;
import com.liangmu.arcvaluecalc.service.ValueService;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = ArcValueCalc.MOD_ID, value = Dist.CLIENT)
public final class ClientRecipeEvents {
    private ClientRecipeEvents() {
    }

    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            ValueService.get().reload(event.getRecipeManager(), minecraft.level.registryAccess(), false);
        }
    }
}
