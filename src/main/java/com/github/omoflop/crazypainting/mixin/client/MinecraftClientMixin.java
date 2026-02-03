package com.github.omoflop.crazypainting.mixin.client;

import com.github.omoflop.crazypainting.client.texture.CanvasTextureManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Inject(method = "clearDownloadedResourcePacks", at = @At("HEAD"))
    public void onDisconnect(CallbackInfo ci) {
        CanvasTextureManager.unloadAll();
    }

}
