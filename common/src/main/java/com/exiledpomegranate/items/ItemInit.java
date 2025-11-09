package com.exiledpomegranate.items;

import com.exiledpomegranate.blocks.BlockInit;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import static com.exiledpomegranate.BarrelBombs.MANAGER;
import static com.exiledpomegranate.BarrelBombs.MOD_ID;

public class ItemInit {
    public static final Registrar<Item> ITEMS = MANAGER.get().get(Registries.ITEM);

    public static final RegistrySupplier<Item> BARRELBOMBITEM = ITEMS.register(new Identifier(MOD_ID, "barrel_bomb_item"),
            () -> new BlockItem(BlockInit.BARREL_BOMB_BLOCK.get(), new Item.Settings().arch$tab(ItemGroups.REDSTONE)));

    public static void init() {
        // This is here to load the class during startup. If the class is never mentioned, then it doesn't load.
    }
}
