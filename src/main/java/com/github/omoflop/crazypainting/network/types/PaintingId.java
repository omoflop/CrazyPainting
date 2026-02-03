package com.github.omoflop.crazypainting.network.types;

import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;

public record PaintingId(int value) {
    public void writeTo(FriendlyByteBuf buf) {
        buf.writeInt(value);
    }

    public static PaintingId readFrom(FriendlyByteBuf buf) {
        return new PaintingId(buf.readInt());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PaintingId that = (PaintingId) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
