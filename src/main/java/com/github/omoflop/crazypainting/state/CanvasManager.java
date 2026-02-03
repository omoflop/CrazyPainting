package com.github.omoflop.crazypainting.state;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.network.ChangeRecord;
import com.github.omoflop.crazypainting.network.s2c.PaintingCanUpdateS2C;
import com.github.omoflop.crazypainting.network.types.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CanvasManager extends SavedData {
    public static final Codec<CanvasManager> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("next_id").forGetter((self) -> self.nextId)
    ).apply(builder, CanvasManager::new));

    private static final String OLD_ID = "crazypainting:canvas_manager";
    private static final String ID = "crazypainting_canvas_manager";

    public static final HashMap<UUID, ChangeRecord> CHANGE_IDS = new HashMap<>();

    private int nextId;

    public CanvasManager(int nextId) {
        this.nextId = nextId;
    }

    public static CanvasManager createNew() {
        return new CanvasManager(-1);
    }

    public int getNextId() {
        nextId += 1;
        return nextId;
    }

    public static CanvasManager getServerState(MinecraftServer server) {
        SavedDataType<CanvasManager> type = new SavedDataType<>(ID, CanvasManager::createNew, CODEC, null);

        // Migrate old CanvasManager dat file
        {
            Path dataDir = server.getWorldPath(LevelResource.ROOT).resolve("data");
            File oldFile = new File(dataDir.toFile(), OLD_ID + ".dat"); // Need to use lenient java.io.File API for potentially illegal names
            Path newFile = dataDir.resolve(ID + ".dat");

            if (oldFile.exists() && !Files.exists(newFile)) {
                CrazyPainting.LOGGER.info("Found old CanvasManager dat file, attempting to migrate");
                try {
                    Files.move(oldFile.toPath(), newFile);
                    CrazyPainting.LOGGER.info("Successfully migrated CanvasManager dat to new filename");
                } catch (IOException e) {
                    CrazyPainting.LOGGER.error("Failed to migrate CanvasManager dat file, sticking with old one", e);
                    type = new SavedDataType<>(OLD_ID, CanvasManager::createNew, CODEC, null);
                }
            }
        }

        CanvasManager canvasManager = Objects.requireNonNull(server.getLevel(Level.OVERWORLD)).getDataStorage().computeIfAbsent(type);
        canvasManager.setDirty();
        return canvasManager;
    }

    public static PaintingData createOrLoad(int canvasId, PaintingSize size, MinecraftServer server) throws IOException {
        PaintingData data = load(canvasId, server);
        if (data == null) return new PaintingData(createEmptyImage(size), size, new PaintingId(canvasId));
        return data;
    }

    public static @Nullable PaintingData load(int canvasId, MinecraftServer server) throws IOException {
        Path savePath = server.getWorldPath(LevelResource.ROOT).resolve("data/crazy_paintings");
        Files.createDirectories(savePath);
        Path paintingPath = savePath.resolve("painting_" + canvasId + ".qoi");

        Optional<byte[]> bytes = tryRead(paintingPath);
        if (bytes.isEmpty()) return null;

        BufferedImage img = ImageIO.read(paintingPath.toFile());
        CrazyPainting.debug("Loaded painting id: {} of size {} x {}", canvasId, img.getWidth(), img.getHeight());
        PaintingSize size = PaintingSize.fromPixels(img.getWidth(), img.getHeight());
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

    public static void receive(PaintingData data, MinecraftServer server, ServerPlayer sender) throws IOException {
        Path savePath = server.getWorldPath(LevelResource.ROOT).resolve("data/crazy_paintings");
        Files.createDirectories(savePath);
        Path paintingPath = savePath.resolve("painting_" + data.id().value() + ".qoi");

        Files.write(paintingPath, data.data());

        for (ServerPlayer player : sender.level().players()) {
            if (player.equals(sender)) continue;
            ServerPlayNetworking.send(player, new PaintingCanUpdateS2C(data.id().value()));
        }
        CrazyPainting.debug("Sent out an update packet to all players except one");
    }

}
