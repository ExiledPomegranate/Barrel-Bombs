package com.exiledpomegranate.items;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.HashMap;
import java.util.Map;

public class Additives {
    public static final HashMap<String, Additives> map = new HashMap<>();

    public final Item targetItem;
    public final int slots;
    private final float powerAdd;
    private final float powerMul;
    private final float powerExp;
    private final float dirOffset;

    private Additives(Item targetItem, int slots, float powerAdd, float powerMul, float powerExp, float dirOffset) {
        this.targetItem = targetItem;
        this.slots = slots;
        this.powerAdd = powerAdd;
        this.powerMul = powerMul;
        this.powerExp = powerExp;
        this.dirOffset = dirOffset;

        map.put(targetItem.getTranslationKey(), this);
    }

    public static float modifyPower(Map<String, Integer> additives, float power) {
        for (Map.Entry<String, Integer> additive : additives.entrySet()) {
            Additives type = map.get(additive.getKey());
            power += (float) (type.powerMul * Math.pow(additive.getValue(), type.powerExp) + type.powerAdd);
        }
        System.out.println(power);
        return power;
    }

    public static int modifyDirectionalOffset(Map<String, Integer> additives, int offset) {
        for (Map.Entry<String, Integer> additive : additives.entrySet()) {
            Additives type = map.get(additive.getKey());
            offset += (int) Math.ceil(additive.getValue() * type.dirOffset);
        }
        System.out.println(offset);
        return offset;
    }

    public static void init() {
        if (map.isEmpty()) {
            new AdditivesBuilder(Items.GUNPOWDER).power(0F, 0.6F, 0.9F).build();
            new AdditivesBuilder(Items.BLAZE_POWDER).directionalOffset(0.5F).build();
        }
    }

    public static class AdditivesBuilder {
        private final Item targetItem;
        private int slots = 1;
        private float powerAdd = 0;
        private float powerMul = 0;
        private float powerExp = 0;
        private float dirOffset = 0;

        public AdditivesBuilder(Item targetItem) {
            this.targetItem = targetItem;
        }

        public AdditivesBuilder slots(int slots) {
            this.slots = slots;
            return this;
        }

        public AdditivesBuilder power(float add, float mul, float exp) {
            this.powerAdd = add;
            this.powerMul = mul;
            this.powerExp = exp;
            return this;
        }

        public AdditivesBuilder directionalOffset(float effectiveness) {
            this.dirOffset = effectiveness;
            return this;
        }

        public Additives build() {
            return new Additives(targetItem, this.slots, this.powerAdd, this.powerMul, this.powerExp, this.dirOffset);
        }
    }
}