package com.github.omoflop.crazypainting.state;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.network.ChangeRecord;
import com.github.omoflop.crazypainting.network.s2c.PaintingCanUpdateS2C;
import com.github.omoflop.crazypainting.network.types.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CanvasManager extends PersistentState {
    public static final Codec<CanvasManager> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("next_id").forGetter((self) -> self.nextId)
    ).apply(builder, CanvasManager::new));

    public static final HashMap<UUID, ChangeRecord> CHANGE_IDS = new HashMap<>();

    private int nextId = -1;

    public CanvasManager(int nextId) {
        this.nextId = nextId;
    }

    public static PaintingData createOrLoad(int canvasId, PaintingSize size, MinecraftServer server) throws IOException {
        PaintingData data = load(canvasId, server);
        if (data == null) return new PaintingData(createEmptyImage(size), size, new PaintingId(canvasId));
        return data;
    }

    public static @Nullable PaintingData load(int canvasId, MinecraftServer server) throws IOException {
        Path savePath = server.getSavePath(WorldSavePath.ROOT).resolve("data/crazy_paintings");
        Files.createDirectories(savePath);
        Path paintingPath = savePath.resolve("painting_" + canvasId + ".qoi");

        Optional<byte[]> bytes = tryRead(paintingPath);
        if (bytes.isEmpty()) return null;

        BufferedImage img = ImageIO.read(paintingPath.toFile());
        PaintingSize size = PaintingSize.fromPixels(img.getWidth() / 16, img.getHeight() / 16);
        return new PaintingData(bytes.get(), size, new PaintingId(canvasId));

    }

    private static Optional<byte[]> tryRead(Path path) {
        try {
            return Optional.of(Files.readAllBytes(path));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static byte[] createEmptyImage(PaintingSize size) {
        BufferedImage img = new BufferedImage(size.width() * 16, size.height() * 16, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[img.getWidth() * img.getHeight()];
        Arrays.fill(pixels, CrazyPainting.WHITE);
        img.setRGB(0, 0, size.width() * 16, size.height() * 16, pixels, 0, size.width()*16);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(img, "qoi", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void receive(PaintingData data, MinecraftServer server, ServerPlayerEntity sender) throws IOException {
        Path savePath = server.getSavePath(WorldSavePath.ROOT).resolve("data/crazy_paintings");
        Files.createDirectories(savePath);
        Path paintingPath = savePath.resolve("painting_" + data.id().value() + ".qoi");

        Files.write(paintingPath, data.data());

        for (ServerPlayerEntity player : sender.getWorld().getPlayers()) {
            if (player.equals(sender)) continue;
            ServerPlayNetworking.send(player, new PaintingCanUpdateS2C(data.id().value()));
        }
        System.out.println("Sent out can update packet to all players except One");
    }

    public int getNextId() {
        nextId += 1;
        return nextId;
    }

    public static CanvasManager createNew() {
        return new CanvasManager(-1);
    }

    public static CanvasManager getServerState(MinecraftServer server) {
        PersistentStateManager manager = Objects.requireNonNull(server.getWorld(World.OVERWORLD)).getPersistentStateManager();
        PersistentStateType<CanvasManager> type = new PersistentStateType<>("crazypainting:canvas_manager", CanvasManager::createNew, CODEC, null);
        CanvasManager canvasManager = manager.getOrCreate(type);
        canvasManager.markDirty();
        return canvasManager;
    }
}
