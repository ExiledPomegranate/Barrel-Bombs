package com.exiledpomegranate.blocks;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import static com.exiledpomegranate.BarrelBombs.MANAGER;
import static com.exiledpomegranate.BarrelBombs.MOD_ID;

public class BlockInit {
    public static Registrar<Block> BLOCKS = MANAGER.get().get(Registries.BLOCK);

    public static RegistrySupplier<Block> BARRELBOMBBLOCK =
            BLOCKS.register(new Identifier(MOD_ID, "barrel_bomb_block"), BarrelBombBlock::new);

    public static void init() {
        // This is here to load the class during startup. If the class is never mentioned, then it doesn't load.
    }
}