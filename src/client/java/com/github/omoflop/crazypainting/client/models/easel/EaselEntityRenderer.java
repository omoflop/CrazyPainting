package com.github.omoflop.crazypainting.client.models.easel;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.client.CrazyPaintingClient;
import com.github.omoflop.crazypainting.client.models.CanvasFeatureRenderer;
import com.github.omoflop.crazypainting.entities.CanvasEaselEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class EaselEntityRenderer extends LivingEntityRenderer<CanvasEaselEntity, EaselEntityRenderState, EaselEntityModel> {
    private static final Identifier textureId = CrazyPainting.id("textures/entity/easel/wood.png");

    public EaselEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new EaselEntityModel(context.getPart(CrazyPaintingClient.EASEL_MODEL_LAYER)), 0.0F);
        this.addFeature(new CanvasFeatureRenderer<>(this));

    }

    @Override
    public EaselEntityRenderState createRenderState() {
        return new EaselEntityRenderState();
    }

    @Override
    public void updateRenderState(CanvasEaselEntity entity, EaselEntityRenderState state, float tickProgress) {
        super.updateRenderState(entity, state, tickProgress);
        state.yaw = MathHelper.lerpAngleDegrees(tickProgress, entity.lastYaw, entity.getYaw());
        state.timeSinceLastHit = (float)(entity.getWorld().getTime() - entity.lastHitTime) + tickProgress;
        state.displayItem = entity.getDisplayStack();
        this.itemModelResolver.updateForLivingEntity(state.displayItemState, state.displayItem, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, entity);
    }

    @Override
    protected void setupTransforms(EaselEntityRenderState state, MatrixStack matrixStack, float bodyYaw, float baseHeight) {
        super.setupTransforms(state, matrixStack, bodyYaw, baseHeight);
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0));
        if (state.timeSinceLastHit < 5.0F) {
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.sin(state.timeSinceLastHit / 1.5F * (float)Math.PI) * 3.0F));
        }
    }

    @Override
    public void render(EaselEntityRenderState state, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(state, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    protected boolean hasLabel(CanvasEaselEntity entity, double d) {
        return entity.hasStackEquipped(EquipmentSlot.MAINHAND);
    }

    @Override
    public Identifier getTexture(EaselEntityRenderState state) {
        return textureId;
    }
}
