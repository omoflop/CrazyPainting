package com.github.omoflop.crazypainting.client.network;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.client.texture.CanvasTextureManager;
import com.github.omoflop.crazypainting.network.s2c.PaintingCanUpdateS2C;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class PaintingCanUpdateHandler implements ClientPlayNetworking.PlayPayloadHandler<PaintingCanUpdateS2C> {
    @Override
    public void receive(PaintingCanUpdateS2C packet, ClientPlayNetworking.Context context) {
        CanvasTextureManager.markAsUpdatable(packet.id());

        CrazyPainting.debug("Received painting can update packet, for id: {}", packet.id());
    }
}
