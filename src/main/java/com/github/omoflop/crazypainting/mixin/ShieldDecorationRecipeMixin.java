package com.github.omoflop.crazypainting.mixin;

import com.github.omoflop.crazypainting.content.CrazyComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShieldDecorationRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ShieldDecorationRecipe.class)
public class ShieldDecorationRecipeMixin {

    @Inject(method = "matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void matchesInjection(CraftingInput craftingRecipeInput, Level arg1, CallbackInfoReturnable<Boolean> cir, boolean bl, boolean bl2, int i, ItemStack itemStack) {
        // Don't allow banners to be added to shields with canvas data!
        if (itemStack.get(CrazyComponents.CANVAS_DATA) != null) {
            cir.setReturnValue(false);
        }
    }

}
