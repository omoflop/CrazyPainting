package com.github.omoflop.crazypainting.client.texture;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.items.CanvasItem;
import com.github.omoflop.crazypainting.network.c2s.RequestPaintingC2S;
import com.github.omoflop.crazypainting.network.types.PaintingData;
import com.github.omoflop.crazypainting.network.types.PaintingSize;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CanvasTextureManager {
    private static final Int2ObjectMap<CanvasTexture> TEXTURE_MAP = new Int2ObjectOpenHashMap<>();
    private static final List<Integer> AWAITED_TEXTURES = new ArrayList<>();
    private static final List<Integer> UPDATABLE_PAINTING_IDS = new ArrayList<>();

    public static Optional<CanvasTexture> request(int id) {
        if (!UPDATABLE_PAINTING_IDS.contains(id)) {
            if (TEXTURE_MAP.containsKey(id)) return Optional.of(TEXTURE_MAP.get(id));
            if (AWAITED_TEXTURES.contains(id)) return Optional.empty();
        }

        AWAITED_TEXTURES.add(id);
        UPDATABLE_PAINTING_IDS.remove((Object) id);
        ClientPlayNetworking.send(new RequestPaintingC2S(id));
        CrazyPainting.debug("Sent request for painting id '{}'", id);

        return Optional.ofNullable(TEXTURE_MAP.get(id));
    }

    public static void unload(int canvasId) {
        if (!TEXTURE_MAP.containsKey(canvasId)) return;
        TEXTURE_MAP.remove(canvasId).close();
    }

    public static void unloadAll() {
        TEXTURE_MAP.values().forEach(CanvasTexture::close);
        AWAITED_TEXTURES.clear();
        TEXTURE_MAP.clear();
    }

    public static CanvasTexture get(int canvasId) {
        return TEXTURE_MAP.get(canvasId);
    }

    private static CanvasTexture updateOrCreate(int canvasId, PaintingData data) {
        CanvasTexture texture;
        if (!TEXTURE_MAP.containsKey(canvasId)) {
            PaintingSize size = data.size();
            texture = new CanvasTexture(size.width(), size.height(), canvasId, true);
            TEXTURE_MAP.put(canvasId, texture);
        } else {
            texture = TEXTURE_MAP.get(canvasId);
        }

        try {
            texture.updateTexture(data.readPixelArray());
        } catch (IOException e) {
            CrazyPainting.debug("Failed to update texture for painting id '{}', {}", canvasId, e);
        }

        return texture;
    }

    public static CanvasTexture receive(int canvasId, PaintingData data) {
        // Remove from awaited textures if it was requested already
        if (AWAITED_TEXTURES.contains(canvasId)) {
            AWAITED_TEXTURES.remove((Object)canvasId);
        }

        CanvasTexture texture = updateOrCreate(canvasId, data);
        texture.updateTexture();
        return texture;
    }

    public static void markAsUpdatable(int id) {
        UPDATABLE_PAINTING_IDS.add(id);
    }
}
