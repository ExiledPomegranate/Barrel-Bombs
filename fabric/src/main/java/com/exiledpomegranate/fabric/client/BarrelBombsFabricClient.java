package com.exiledpomegranate.fabric.client;

import com.exiledpomegranate.BarrelBombsClient;
import net.fabricmc.api.ClientModInitializer;

public final class BarrelBombsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BarrelBombsClient.init();
    }
}
