package com.github.omoflop.crazypainting.network.s2c;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.network.types.PaintingData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PaintingUpdateS2C(int id, PaintingData data) implements CustomPacketPayload {
    public static final Type<PaintingUpdateS2C> ID = new Type<>(CrazyPainting.id("painting_update"));
    public static final StreamCodec<FriendlyByteBuf, PaintingUpdateS2C> CODEC = StreamCodec.ofMember(PaintingUpdateS2C::encode, PaintingUpdateS2C::decode);

    private void encode(FriendlyByteBuf buf) {
        buf.writeInt(id);
        data.writeTo(buf);
    }

    private static PaintingUpdateS2C decode(FriendlyByteBuf buf) {
        return new PaintingUpdateS2C(buf.readInt(), PaintingData.readFrom(buf));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
