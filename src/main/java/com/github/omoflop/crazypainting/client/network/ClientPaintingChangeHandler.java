package com.github.omoflop.crazypainting.client.network;

import com.github.omoflop.crazypainting.client.screens.PaintingEditorScreen;
import com.github.omoflop.crazypainting.client.texture.CanvasTexture;
import com.github.omoflop.crazypainting.client.texture.CanvasTextureManager;
import com.github.omoflop.crazypainting.client.screens.PaintingViewerScreen;
import com.github.omoflop.crazypainting.network.event.PaintingChangeEvent;
import com.github.omoflop.crazypainting.network.types.ChangeKey;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import java.util.Arrays;

import static com.github.omoflop.crazypainting.CrazyPainting.LOGGER;
import static com.github.omoflop.crazypainting.CrazyPainting.SHOW_DEBUG_LOGS;

public class ClientPaintingChangeHandler implements ClientPlayNetworking.PlayPayloadHandler<PaintingChangeEvent> {
    @Override
    public void receive(PaintingChangeEvent packet, ClientPlayNetworking.Context context) {
        if (SHOW_DEBUG_LOGS) {
            LOGGER.info( "Received painting change");

            LOGGER.info("Change present? {}", packet.change().isPresent());
            if (packet.change().isPresent()) {
                ChangeKey key = packet.change().get();
                LOGGER.info("\t key:{}", Arrays.toString(key.key()));
            }

            LOGGER.info(Arrays.toString(packet.data().data()));
        }

        if (packet.change().isPresent()) {
            CanvasTexture texture = CanvasTextureManager.receive(packet.data().id().value(), packet.data());
            Minecraft.getInstance().setScreen(new PaintingEditorScreen(packet, texture, context.player(), packet.easelEntityId()));
        } else {
            Minecraft.getInstance().setScreen(new PaintingViewerScreen(packet.data(), packet.title()));
        }
    }
}
