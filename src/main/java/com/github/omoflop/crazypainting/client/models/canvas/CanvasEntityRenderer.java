package com.github.omoflop.crazypainting.client.models.canvas;

import com.github.omoflop.crazypainting.entities.CanvasEntity;
import com.github.omoflop.crazypainting.items.CanvasItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CanvasEntityRenderer extends EntityRenderer<CanvasEntity, CanvasEntityRenderState> {
    private final ItemModelResolver itemModelResolver;

    public CanvasEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemModelResolver = context.getItemModelResolver();

    }

    @Override
    public CanvasEntityRenderState createRenderState() {
        return new CanvasEntityRenderState();
    }

    public void extractRenderState(CanvasEntity entity, CanvasEntityRenderState state, float tickProgress) {
        super.extractRenderState(entity, state, tickProgress);
        state.facing = entity.getDirection();
        ItemStack canvasItem = entity.getEntityData().get(CanvasEntity.CANVAS_ITEM);
        if (!(canvasItem.getItem() instanceof CanvasItem)) return;
        state.rotation = entity.getEntityData().get(CanvasEntity.ROTATION);
        state.glow = CanvasItem.getGlow(canvasItem);
        state.displayItem = canvasItem;
        itemModelResolver.updateForNonLiving(state.displayItemState, state.displayItem, ItemDisplayContext.FIXED, entity);

    }

    @Override
    public void submit(CanvasEntityRenderState state, PoseStack matrices, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (!(state.displayItem.getItem() instanceof CanvasItem canvas)) return;


        matrices.pushPose();
        float f;
        float g;
        if (state.facing.getAxis().isHorizontal()) {
            f = 0.0f;
            g = 180.0f - state.facing.toYRot();
        } else {
            f = -90 * state.facing.getAxisDirection().getStep();
            g = 180.0f;
        }
        matrices.mulPose(Axis.XP.rotationDegrees(f));
        matrices.mulPose(Axis.YP.rotationDegrees(g));

        double distance = Minecraft.getInstance().getCameraEntity().distanceToSqr(state.x, state.y, state.z);


        matrices.translate(0,0, Math.min(32f/distance, 1.38/16f));

        // undo ui shrink lol
        int biggest = Math.max(canvas.width, canvas.height);
        matrices.scale(biggest, biggest, 1);

        // Rotate!
        matrices.mulPose(Axis.ZP.rotationDegrees(state.rotation*90));

        float scale = 1.03f;

        state.displayItemState.submit(matrices, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        matrices.popPose();

        super.submit(state, matrices, submitNodeCollector, cameraRenderState);
    }
}
