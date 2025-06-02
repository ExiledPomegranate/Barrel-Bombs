package com.exiledpomegranate.entities;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import static com.exiledpomegranate.BarrelBombs.MANAGER;
import static com.exiledpomegranate.BarrelBombs.MOD_ID;

public class EntityInit {
    public static final RegistrySupplier<EntityType<BarrelBombEntity>> BARREL_BOMB_ENTITY =
            MANAGER.get().get(RegistryKeys.ENTITY_TYPE)
                    .register(new Identifier(MOD_ID, "barrel_bomb_entity"),
                            () -> EntityType.Builder.<BarrelBombEntity>create(BarrelBombEntity::new, SpawnGroup.MISC)
                                    .setDimensions(1F, 1F)
                                    .build("barrel_bomb_entity"));

    public static void init() {
        // This is here to load the class during startup. If the class is never mentioned, then it doesn't load.
    }
}