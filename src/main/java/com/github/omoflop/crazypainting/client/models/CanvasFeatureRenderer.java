package com.github.omoflop.crazypainting.client.models;

import com.github.omoflop.crazypainting.items.CanvasItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

@Environment(EnvType.CLIENT)
public class CanvasFeatureRenderer<S extends EntityRenderState & ItemDisplaying, M extends EntityModel<S>> extends RenderLayer<S, M> {
    public CanvasFeatureRenderer(RenderLayerParent<S, M> ctx) {
        super(ctx);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S state, float f, float g) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.mulPose(Axis.XP.rotationDegrees(5.5f));
        poseStack.translate(0, -0.8, -.110);
        poseStack.scale(1.25f, 1.25f, 1.25f);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        if (state.getDisplayItem().getItem() instanceof CanvasItem) {
            poseStack.scale(2, 2, .66f);
        }

        state.getDisplayItemState().submit(poseStack, submitNodeCollector, i, OverlayTexture.NO_OVERLAY, 1);
        poseStack.popPose();
    }

}
