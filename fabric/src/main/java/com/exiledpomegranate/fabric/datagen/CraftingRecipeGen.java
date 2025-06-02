package com.exiledpomegranate.fabric.datagen;

import com.exiledpomegranate.items.ItemInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

import static com.exiledpomegranate.BarrelBombs.MOD_ID;

public class CraftingRecipeGen extends FabricRecipeProvider {
    public CraftingRecipeGen(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(Consumer<RecipeJsonProvider> exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ItemInit.BARRELBOMBITEM.get())
                .pattern("PGP")
                .pattern("GBG")
                .pattern("PGP")
                .input('G', Items.GUNPOWDER)
                .input('P', Items.BLAZE_POWDER)
                .input('B', Blocks.BARREL)
                .criterion("has_barrel", conditionsFromItem(Blocks.BARREL))
                .offerTo(exporter, new Identifier(MOD_ID, "barrel_bomb_crafting"));
    }
}
