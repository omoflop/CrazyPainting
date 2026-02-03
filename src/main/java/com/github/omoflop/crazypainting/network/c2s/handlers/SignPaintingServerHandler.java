package com.github.omoflop.crazypainting.network.c2s.handlers;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.components.CanvasDataComponent;
import com.github.omoflop.crazypainting.content.CrazyComponents;
import com.github.omoflop.crazypainting.entities.CanvasEaselEntity;
import com.github.omoflop.crazypainting.network.c2s.SignPaintingC2S;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class SignPaintingServerHandler implements ServerPlayNetworking.PlayPayloadHandler<SignPaintingC2S> {
    @Override
    public void receive(SignPaintingC2S packet, ServerPlayNetworking.Context ctx) {
        ServerPlayerEntity player = ctx.player();

        Entity entity = player.getWorld().getEntityById(packet.easelEntityId());
        if (!(entity instanceof CanvasEaselEntity easel)) {
            if (CrazyPainting.SHOW_DEBUG_LOGS) CrazyPainting.LOGGER.warn("Received invalid entity id for sign painting packet");
            return;
        }

        ItemStack displayStack = easel.getDisplayStack();
        CanvasDataComponent data = displayStack.get(CrazyComponents.CANVAS_DATA);
        if (data == null) {
            if (CrazyPainting.SHOW_DEBUG_LOGS) CrazyPainting.LOGGER.warn("Tried to sign a canvas with no data");
            return;
        }

        displayStack.set(CrazyComponents.CANVAS_DATA, data.withSignedBy(player.getNameForScoreboard()).withTitle(packet.title()).withGeneration((byte)0));
        easel.setDisplayStack(displayStack);
    }
}