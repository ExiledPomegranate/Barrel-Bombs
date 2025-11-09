package com.exiledpomegranate.blocks;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import static com.exiledpomegranate.BarrelBombs.MANAGER;
import static com.exiledpomegranate.BarrelBombs.MOD_ID;

public class BlockInit {
    public static final Registrar<Block> BLOCKS = MANAGER.get().get(Registries.BLOCK);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<Block> BARREL_BOMB_BLOCK =
            BLOCKS.register(new Identifier(MOD_ID, "barrel_bomb_block"), BarrelBombBlock::new);

    public static final RegistrySupplier<BlockEntityType<BarrelBombBlockEntity>> BARREL_BOMB_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("barrel_bomb_block_entity", () -> BlockEntityType.Builder.create(BarrelBombBlockEntity::new, BARREL_BOMB_BLOCK.get()).build(null));

    public static void init() {
        // This is here to load the class during startup. If the class is never mentioned, then it doesn't load.
    }
}