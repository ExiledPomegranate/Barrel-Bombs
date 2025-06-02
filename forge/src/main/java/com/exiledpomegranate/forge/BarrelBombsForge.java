package com.exiledpomegranate.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.exiledpomegranate.BarrelBombs;

@Mod(BarrelBombs.MOD_ID)
public final class BarrelBombsForge {
    public BarrelBombsForge() {
        EventBuses.registerModEventBus(BarrelBombs.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        BarrelBombs.init();
    }
}
