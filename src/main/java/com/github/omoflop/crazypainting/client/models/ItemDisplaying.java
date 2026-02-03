package com.github.omoflop.crazypainting.client.models;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;

public interface ItemDisplaying {
    ItemStack getDisplayItem();
    ItemStackRenderState getDisplayItemState();
}
