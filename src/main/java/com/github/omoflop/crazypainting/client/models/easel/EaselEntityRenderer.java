package com.github.omoflop.crazypainting.client.models.easel;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.client.CrazyPaintingClient;
import com.github.omoflop.crazypainting.client.models.CanvasFeatureRenderer;
import com.github.omoflop.crazypainting.entities.CanvasEaselEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;

public class EaselEntityRenderer extends LivingEntityRenderer<CanvasEaselEntity, EaselEntityRenderState, EaselEntityModel> {
    private static final Identifier textureId = CrazyPainting.id("textures/entity/easel/wood.png");

    public EaselEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new EaselEntityModel(context.bakeLayer(CrazyPaintingClient.EASEL_MODEL_LAYER)), 0.0F);
        this.addLayer(new CanvasFeatureRenderer<>(this));

    }

    @Override
    public EaselEntityRenderState createRenderState() {
        return new EaselEntityRenderState();
    }

    @Override
    public void extractRenderState(CanvasEaselEntity entity, EaselEntityRenderState state, float tickProgress) {
        super.extractRenderState(entity, state, tickProgress);
        state.timeSinceLastHit = (float)(entity.level().getGameTime() - entity.lastHitTime) + tickProgress;
        state.displayItem = entity.getDisplayStack();
        this.itemModelResolver.updateForLiving(state.displayItemState, state.displayItem, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, entity);
    }

    @Override
    protected void setupRotations(EaselEntityRenderState state, PoseStack matrixStack, float bodyYaw, float baseHeight) {
        super.setupRotations(state, matrixStack, bodyYaw, baseHeight);
        matrixStack.mulPose(Axis.YP.rotationDegrees(0));
        if (state.timeSinceLastHit < 5.0F) {
            matrixStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(state.timeSinceLastHit / 1.5F * (float)Math.PI) * 3.0F));
        }
    }




    @Override
    protected boolean shouldShowName(CanvasEaselEntity entity, double d) {
        return entity.hasItemInSlot(EquipmentSlot.MAINHAND);
    }

    @Override
    public Identifier getTextureLocation(EaselEntityRenderState state) {
        return textureId;
    }

}
