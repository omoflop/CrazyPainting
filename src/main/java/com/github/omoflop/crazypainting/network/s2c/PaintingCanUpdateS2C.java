package com.github.omoflop.crazypainting.network.s2c;

import com.github.omoflop.crazypainting.CrazyPainting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PaintingCanUpdateS2C(int id) implements CustomPacketPayload {
    public static final Type<PaintingCanUpdateS2C> ID = new Type<>(CrazyPainting.id("painting_can_update"));
    public static final StreamCodec<FriendlyByteBuf, PaintingCanUpdateS2C> CODEC = StreamCodec.ofMember(PaintingCanUpdateS2C::encode, PaintingCanUpdateS2C::decode);

    private void encode(FriendlyByteBuf buf) {
        buf.writeInt(id);
    }

    private static PaintingCanUpdateS2C decode(FriendlyByteBuf buf) {
        return new PaintingCanUpdateS2C(buf.readInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}