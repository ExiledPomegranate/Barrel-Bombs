package com.exiledpomegranate.blocks;

import com.exiledpomegranate.items.Additives;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BarrelBombBlockEntity extends BlockEntity {
    private final Map<String, Integer> additives = new HashMap<>();

    public BarrelBombBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.BARREL_BOMB_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        NbtCompound additivesNBT = new NbtCompound();
        for (Map.Entry<String, Integer> additive : additives.entrySet()) {
            additivesNBT.putInt(additive.getKey(), additive.getValue());
        }
        nbt.put("additives", additivesNBT);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        NbtCompound additivesNBT = nbt.getCompound("additives");
        for (String key : additivesNBT.getKeys()) {
            additives.put(key, additivesNBT.getInt(key));
        }
    }

    public Map<String, Integer> getAdditives() {
        return Collections.unmodifiableMap(additives);
    }

    public int getAdditive(Additives additive) {
        return additives.getOrDefault(additive.targetItem.getTranslationKey(), 0);
    }

    public void setAdditives(Additives key, int value) {
        additives.put(key.targetItem.getTranslationKey(), value);
        this.markDirty();
    }
}