package com.github.omoflop.crazypainting.client.models.canvas;

import com.github.omoflop.crazypainting.components.CanvasDataComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class CanvasEntityRenderState extends EntityRenderState {
    public Direction facing = Direction.NORTH;
    public byte rotation;
    public boolean glow;
    public ItemStack displayItem = ItemStack.EMPTY;
    public ItemStackRenderState displayItemState = new ItemStackRenderState();
}
