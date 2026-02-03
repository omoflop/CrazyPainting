package com.github.omoflop.crazypainting.content;

import com.github.omoflop.crazypainting.network.c2s.SignPaintingC2S;
import com.github.omoflop.crazypainting.network.c2s.handlers.SignPaintingServerHandler;
import com.github.omoflop.crazypainting.network.s2c.PaintingCanUpdateS2C;
import com.github.omoflop.crazypainting.network.s2c.UpdateEaselCanvasIdS2C;
import com.github.omoflop.crazypainting.network.types.PaintingData;
import com.github.omoflop.crazypainting.network.types.PaintingSize;
import com.github.omoflop.crazypainting.network.event.PaintingChangeEvent;
import com.github.omoflop.crazypainting.network.c2s.RequestPaintingC2S;
import com.github.omoflop.crazypainting.network.event.ServerPaintingChangeEventHandler;
import com.github.omoflop.crazypainting.network.c2s.handlers.RequestPaintingServerHandler;
import com.github.omoflop.crazypainting.network.s2c.PaintingUpdateS2C;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CrazyNetworking {
    public static void register() {
        // Server -> Client
        PayloadTypeRegistry.playS2C().register(PaintingChangeEvent.ID, PaintingChangeEvent.CODEC);
        PayloadTypeRegistry.playS2C().register(PaintingUpdateS2C.ID, PaintingUpdateS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(PaintingCanUpdateS2C.ID, PaintingCanUpdateS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateEaselCanvasIdS2C.ID, UpdateEaselCanvasIdS2C.CODEC);

        // Client -> Server
        PayloadTypeRegistry.playC2S().register(PaintingChangeEvent.ID, PaintingChangeEvent.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestPaintingC2S.ID, RequestPaintingC2S.CODEC);
        PayloadTypeRegistry.playC2S().register(SignPaintingC2S.ID, SignPaintingC2S.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(PaintingChangeEvent.ID, new ServerPaintingChangeEventHandler());
        ServerPlayNetworking.registerGlobalReceiver(RequestPaintingC2S.ID, new RequestPaintingServerHandler());
        ServerPlayNetworking.registerGlobalReceiver(SignPaintingC2S.ID, new SignPaintingServerHandler());
    }

    public static void savePainting(Path savePath, int id, PaintingData data, PaintingSize size) throws IOException {
        Files.createDirectory(savePath);
        File pngFile = Files.createFile(savePath.resolve("painting_" + id + ".png")).toFile();

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(data.data()));
        ImageIO.write(image, "PNG", pngFile);
    }

    public static byte[] loadPainting(Path rootPath, int id) throws IOException {
        return Files.readAllBytes(rootPath.resolve("painting_" + id + ".png"));
    }

    public static byte[] readByteArray(FriendlyByteBuf buf) {
        int length = buf.readInt();

        ByteBuf bb = buf.readBytes(length);
        byte[] arr = new byte[bb.readableBytes()];
        bb.readBytes(arr);

        return arr;
    }

    public static void writeByteArray(FriendlyByteBuf buf, byte[] bytes) {
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }
}
