package com.liangmu.arcvaluecalc;

import com.liangmu.arcvaluecalc.command.ArcValueCommands;
import com.liangmu.arcvaluecalc.config.ArcValueConfig;
import com.liangmu.arcvaluecalc.network.ArcValueNetwork;
import com.liangmu.arcvaluecalc.service.ValueService;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ArcValueCalc.MOD_ID)
public final class ArcValueCalc {
    public static final String MOD_ID = "arcvaluecalc";

    public ArcValueCalc() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ArcValueConfig.SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
        MinecraftForge.EVENT_BUS.addListener(this::addReloadListener);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ArcValueNetwork::register);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        ArcValueCommands.register(event.getDispatcher());
    }

    private void serverStarted(ServerStartedEvent event) {
        ValueService.get().reload(event.getServer().getRecipeManager(), event.getServer().registryAccess(), true);
    }

    private void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new RecipeReloadListener());
    }
}
