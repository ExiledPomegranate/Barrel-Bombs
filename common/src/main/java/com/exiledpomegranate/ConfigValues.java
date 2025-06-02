package com.exiledpomegranate;

import com.exiledpomegranate.entities.BarrelBombEntity.SmokeParticles;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.particle.ParticleTypes;

import java.util.ArrayList;
import java.util.List;

public class ConfigValues {
    public float power;
    public float penetration;
    public float dropPercentage;
    public int directionalOffset;
    public int additiveCap;
    public List<SmokeParticles> smokeParticles;
    public List<Block> dropBlacklist;
    public List<Block> immuneList;

    public ConfigValues() {
        power = 4.0F;
        penetration = 1000F;
        dropPercentage = 0.1F;
        directionalOffset = 6;
        additiveCap = 32;
        dropBlacklist = new ArrayList<>();
        dropBlacklist.add(Blocks.STONE);
        dropBlacklist.add(Blocks.DEEPSLATE);
        dropBlacklist.add(Blocks.ANDESITE);
        dropBlacklist.add(Blocks.DIORITE);
        dropBlacklist.add(Blocks.GRANITE);
        dropBlacklist.add(Blocks.CALCITE);
        dropBlacklist.add(Blocks.DIRT);
        dropBlacklist.add(Blocks.TUFF);
        dropBlacklist.add(Blocks.GRAVEL);
        dropBlacklist.add(Blocks.NETHERRACK);
        dropBlacklist.add(Blocks.BASALT);
        dropBlacklist.add(Blocks.BLACKSTONE);
        dropBlacklist.add(Blocks.ANCIENT_DEBRIS);
        immuneList = new ArrayList<>();
        immuneList.add(Blocks.OBSIDIAN);
        immuneList.add(Blocks.CHEST);
        immuneList.add(Blocks.BLACK_BED);
        immuneList.add(Blocks.BLUE_BED);
        immuneList.add(Blocks.BROWN_BED);
        immuneList.add(Blocks.CYAN_BED);
        immuneList.add(Blocks.GRAY_BED);
        immuneList.add(Blocks.GREEN_BED);
        immuneList.add(Blocks.LIGHT_BLUE_BED);
        immuneList.add(Blocks.LIGHT_GRAY_BED);
        immuneList.add(Blocks.LIME_BED);
        immuneList.add(Blocks.MAGENTA_BED);
        immuneList.add(Blocks.PINK_BED);
        immuneList.add(Blocks.PURPLE_BED);
        immuneList.add(Blocks.RED_BED);
        immuneList.add(Blocks.WHITE_BED);
        immuneList.add(Blocks.YELLOW_BED);
        immuneList.add(Blocks.ORANGE_BED);
        immuneList.add(Blocks.BARREL);
        immuneList.add(Blocks.FURNACE);
        immuneList.add(Blocks.CRAFTING_TABLE);
        smokeParticles = new ArrayList<>();
        smokeParticles.add(new SmokeParticles(ParticleTypes.CLOUD, 100, 4F, 0.01F));
        smokeParticles.add(new SmokeParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, 500, 2F, 0.05F));
        smokeParticles.add(new SmokeParticles(ParticleTypes.EXPLOSION_EMITTER, 10, 4F, 0F));
    }
}