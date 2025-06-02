package com.exiledpomegranate.forge;

import com.exiledpomegranate.BarrelBombsClient;
import com.exiledpomegranate.entities.EntityInit;
import com.exiledpomegranate.entities.renderers.BarrelBombEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.exiledpomegranate.BarrelBombs.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BarrelBombsForgeClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            //BarrelBombsClient.init();
            EntityRenderers.register(EntityInit.BARREL_BOMB_ENTITY.get(), BarrelBombEntityRenderer::new);
        });
    }
}
