package com.exiledpomegranate;

import com.exiledpomegranate.entities.EntityInit;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.exiledpomegranate.entities.renderers.BarrelBombEntityRenderer;

@Environment(EnvType.CLIENT)
public class BarrelBombsClient {
    public static void init() {
        EntityRendererRegistry.register(EntityInit.BARREL_BOMB_ENTITY, BarrelBombEntityRenderer::new);
    }
}
