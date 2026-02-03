package com.github.omoflop.crazypainting.mixin.client;

import com.github.omoflop.crazypainting.client.models.canvas.CanvasSpecialRenderer;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpecialModelWrapper.class)
public class SpecialItemModelMixin<T> {

    @Unique
    private boolean crazypainting$isSpecial;

    @ModifyExpressionValue(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/special/SpecialModelRenderer;extractArgument(Lnet/minecraft/world/item/ItemStack;)Ljava/lang/Object;"))
    public T crazypainting$grabData(T data) {
        crazypainting$isSpecial = (data instanceof CanvasSpecialRenderer.Data);
        return data;
    }

    @Inject(method = "update", at = @At(value = "RETURN"))
    public void crazypainting$update(ItemStackRenderState state, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext itemDisplayContext, ClientLevel clientLevel, ItemOwner itemOwner, int i, CallbackInfo ci) {
        if (crazypainting$isSpecial) {
            state.setAnimated();
            state.setOversizedInGui(true);
        }
    }
}
