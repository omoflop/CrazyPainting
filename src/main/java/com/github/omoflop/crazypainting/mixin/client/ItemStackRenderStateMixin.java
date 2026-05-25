package com.github.omoflop.crazypainting.mixin.client;

import com.github.omoflop.crazypainting.client.CrazyPaintingClient;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public class ItemStackRenderStateMixin {
	@WrapOperation(method = "applyTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemDisplayContext;leftHand()Z"))
	private boolean crazyPainting$cacheContext(ItemDisplayContext instance, Operation<Boolean> original) {
		CrazyPaintingClient.displayContext = instance;
		return original.call(instance);
	}

	@Inject(method = "submit", at = @At("TAIL"))
	private void crazyPainting$cacheContext(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, int outlineColor, CallbackInfo ci) {
		CrazyPaintingClient.displayContext = null;
	}
}
