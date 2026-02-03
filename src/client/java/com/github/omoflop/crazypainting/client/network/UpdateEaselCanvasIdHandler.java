package com.github.omoflop.crazypainting.client.network;

import com.github.omoflop.crazypainting.components.CanvasDataComponent;
import com.github.omoflop.crazypainting.content.CrazyComponents;
import com.github.omoflop.crazypainting.entities.CanvasEaselEntity;
import com.github.omoflop.crazypainting.network.s2c.UpdateEaselCanvasIdS2C;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class UpdateEaselCanvasIdHandler implements ClientPlayNetworking.PlayPayloadHandler<UpdateEaselCanvasIdS2C> {
    @Override
    public void receive(UpdateEaselCanvasIdS2C packet, ClientPlayNetworking.Context context) {
        Entity entity = context.player().getWorld().getEntityById(packet.entityId());
        if (entity instanceof CanvasEaselEntity easel) {
            ItemStack stack = easel.getDisplayStack();
            CanvasDataComponent data = CanvasDataComponent.withId(stack.get(CrazyComponents.CANVAS_DATA), packet.id().value());
            stack.set(CrazyComponents.CANVAS_DATA, data);
            easel.setDisplayStack(stack);
        }
    }
}
