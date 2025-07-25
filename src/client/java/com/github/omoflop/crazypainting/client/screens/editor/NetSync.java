package com.github.omoflop.crazypainting.client.screens.editor;

import com.github.omoflop.crazypainting.client.texture.CanvasTexture;
import com.github.omoflop.crazypainting.network.c2s.SignPaintingC2S;
import com.github.omoflop.crazypainting.network.event.PaintingChangeEvent;
import com.github.omoflop.crazypainting.network.types.ChangeKey;
import com.github.omoflop.crazypainting.network.types.PaintingData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.github.omoflop.crazypainting.CrazyPainting.LOGGER;

public class NetSync {
    private final CanvasTexture texture;
    private final Supplier<String> titleGetter;
    private final ChangeKey changeKey;
    private final AtomicBoolean hasChanges;
    private final int easelEntityId;

    private boolean closed = false;

    private int updateCooldown = 0;

    public NetSync(CanvasTexture texture, ChangeKey changeKey, Supplier<String> titleGetter, AtomicBoolean hasChanges, int easelEntityId) {
        this.texture = texture;
        this.changeKey = changeKey;
        this.titleGetter = titleGetter;
        this.hasChanges = hasChanges;
        this.easelEntityId = easelEntityId;
    }

    public void tick() {
        if (!hasChanges.get()) return;

        updateCooldown -= 1;
        if (updateCooldown <= 0) {
            updateCooldown = 20;
            hasChanges.set(false);
            sendChanges();
        }
    }

    public void sendChanges() {
        PaintingData data = texture.toData();
        if (data != null) {
            texture.updateTexture();
            ClientPlayNetworking.send(new PaintingChangeEvent(Optional.of(changeKey), data, titleGetter.get(), easelEntityId));
        } else {
            LOGGER.error("Failed to send painting change update packet");
        }
    }

    public void signAndClose(PaintingData data) {
        texture.updateTexture();
        ClientPlayNetworking.send(new PaintingChangeEvent(Optional.of(changeKey), data, titleGetter.get(), easelEntityId));
        ClientPlayNetworking.send(new SignPaintingC2S(changeKey, data.id(), easelEntityId, titleGetter.get()));

        closed = true;
    }

    public void close() {
        if (closed) return;
        sendChanges();
    }
}
