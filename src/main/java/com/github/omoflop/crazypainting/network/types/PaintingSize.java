package com.github.omoflop.crazypainting.network.types;

import com.github.omoflop.crazypainting.items.CanvasItem;
import net.minecraft.network.FriendlyByteBuf;

public record PaintingSize(byte width, byte height) {

    public static PaintingSize fromPixels(int width, int height) {
        return new PaintingSize((byte) (width / 16), (byte) (height / 16));
    }

    public static PaintingSize from(CanvasItem canvasItem) {
        return new PaintingSize(canvasItem.width, canvasItem.height);
    }

    public void writeTo(FriendlyByteBuf buf) {
        buf.writeByte(pack());
    }

    public static PaintingSize readFrom(FriendlyByteBuf buf) {
        return unpack(buf.readByte());
    }

    private byte pack() {
        return (byte) ((width << 4) | (height & 0x0F));
    }

    private static PaintingSize unpack(byte value) {
        return new PaintingSize(
                (byte) ((value >> 4) & 0x0F),
                (byte) (value & 0x0F)
        );
    }
}