package com.github.omoflop.crazypainting.mixin.client;

import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

@Mixin(TextureManager.class)
public interface TextureManagerAccessor {
    @Intrinsic
    @Accessor("byPath")
    Map<Identifier, AbstractTexture> textures();
}
