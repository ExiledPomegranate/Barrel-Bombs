package com.exiledpomegranate.entities.renderers;

import com.exiledpomegranate.blocks.BlockInit;
import com.exiledpomegranate.entities.BarrelBombEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.TntMinecartEntityRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class BarrelBombEntityRenderer extends EntityRenderer<BarrelBombEntity> {
    private final BlockRenderManager blockRenderManager;

    public BarrelBombEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0F;
        this.blockRenderManager = context.getBlockRenderManager();
    }

    public void render(BarrelBombEntity barrelBomb, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        matrixStack.translate(0.0F, 0.5F, 0.0F);
        int j = barrelBomb.getFuse();
        if ((float)j - g + 1.0F < 10.0F) {
            float h = 1.0F - ((float)j - g + 1.0F) / 10.0F;
            h = MathHelper.clamp(h, 0.0F, 1.0F);
            h *= h;
            h *= h;
            float k = 1.0F + h * 0.3F;
            matrixStack.scale(k, k, k);
        }

        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));
        matrixStack.translate(-0.5F, -0.5F, 0.5F);
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
        TntMinecartEntityRenderer.renderFlashingBlock(this.blockRenderManager,
                BlockInit.BARRELBOMBBLOCK.get().getDefaultState().with(Properties.FACING, barrelBomb.getFacing()),
                matrixStack, vertexConsumerProvider, i, j / 5 % 2 == 0);
        matrixStack.pop();
        super.render(barrelBomb, f, g, matrixStack, vertexConsumerProvider, i);
    }

    public Identifier getTexture(BarrelBombEntity barrelBomb) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}
