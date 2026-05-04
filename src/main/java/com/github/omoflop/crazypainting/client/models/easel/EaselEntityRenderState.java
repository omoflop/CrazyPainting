package com.github.omoflop.crazypainting.client.models.easel;

import com.github.omoflop.crazypainting.client.models.ItemDisplaying;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;

public class EaselEntityRenderState extends LivingEntityRenderState implements ItemDisplaying {
    public float timeSinceLastHit;
    public ItemStack displayItem = ItemStack.EMPTY;
    public ItemStackRenderState displayItemState = new ItemStackRenderState();

    @Override
    public ItemStack getDisplayItem() {
        return displayItem;
    }

    @Override
    public ItemStackRenderState getDisplayItemState() {
        return displayItemState;
    }
}
