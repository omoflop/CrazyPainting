package com.github.omoflop.crazypainting.mixin.client;

import com.github.omoflop.crazypainting.client.models.CanvasRenderer;
import com.github.omoflop.crazypainting.components.CanvasDataComponent;
import com.github.omoflop.crazypainting.content.CrazyComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Optional;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;

@Mixin(ShieldSpecialRenderer.class)
public class ShieldModelRendererMixin {

    @Inject(method = "submit(Lnet/minecraft/core/component/DataComponentMap;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V", at = @At(value = "TAIL"))
    private void render(DataComponentMap componentMap, ItemDisplayContext itemDisplayContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, boolean bl, int k, CallbackInfo ci) {
        CanvasDataComponent data = componentMap.get(CrazyComponents.CANVAS_DATA);
        if (data == null) return;

        Optional<Identifier> textureId = CanvasRenderer.tryGetCanvasId(data.id());
        if (textureId.isEmpty()) return;

        poseStack.pushPose();
        boolean glow = itemDisplayContext == ItemDisplayContext.GUI || data.glow();

        CanvasRenderer.prepareForShield(poseStack);

        submitNodeCollector.submitCustomGeometry(poseStack, CanvasRenderer.getRenderType(textureId.get(), glow), (pose, vertexConsumer) -> {
            CanvasRenderer.submitFront(vertexConsumer, pose, 1, 2, i);
        });

        poseStack.popPose();
    }

}
