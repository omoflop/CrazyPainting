package com.github.omoflop.crazypainting.network.c2s.handlers;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.network.c2s.RequestPaintingC2S;
import com.github.omoflop.crazypainting.network.s2c.PaintingUpdateS2C;
import com.github.omoflop.crazypainting.network.types.PaintingData;
import com.github.omoflop.crazypainting.state.CanvasManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import java.io.IOException;
import java.util.Objects;

public class RequestPaintingServerHandler implements ServerPlayNetworking.PlayPayloadHandler<RequestPaintingC2S> {
    @Override
    public void receive(RequestPaintingC2S packet, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        MinecraftServer server = Objects.requireNonNull(player.level()).getServer();

        PaintingData data;
        try {
            data = CanvasManager.load(packet.id(), server);
            if (data == null) {
                CrazyPainting.debug("Client {} requested painting id '{}' which is not saved to disk. Sending nothing.", ctx.player().getScoreboardName(), packet.id());
            } else {
                CrazyPainting.debug("Sending client {} painting of size: {}", ctx.player().getScoreboardName(), data.size().toString());
                ServerPlayNetworking.send(player, new PaintingUpdateS2C(packet.id(), data));
            }
        } catch (IOException e) {
            CrazyPainting.debug("Failed to load painting from disk after player requested it... {}", e);
        }



    }
}