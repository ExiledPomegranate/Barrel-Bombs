package com.exiledpomegranate;

import com.exiledpomegranate.blocks.BlockInit;
import com.exiledpomegranate.entities.EntityInit;
import com.exiledpomegranate.items.ItemInit;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.RegistrarManager;
import com.google.common.base.Suppliers;

import java.util.function.Supplier;

public final class BarrelBombs {
    public static final String MOD_ID = "barrel_bombs";
    public static final Supplier<RegistrarManager> MANAGER = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    public static void init() {
        ItemInit.init();
        BlockInit.init();
        EntityInit.init();
        ConfigHandler.load(Platform.getConfigFolder());
        LifecycleEvent.SERVER_STOPPING.register(server -> ConfigHandler.save(Platform.getConfigFolder()));
        CommandRegistrationEvent.EVENT.register((dispatcher,
                                                 registryAccess, environment) -> {
            BarrelBombCommand.register(dispatcher);
        });
    }
}
