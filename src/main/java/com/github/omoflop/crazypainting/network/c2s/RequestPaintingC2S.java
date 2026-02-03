package com.github.omoflop.crazypainting.network.c2s;

import com.github.omoflop.crazypainting.CrazyPainting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RequestPaintingC2S(int id) implements CustomPacketPayload {
    public static final Type<RequestPaintingC2S> ID = new Type<>(CrazyPainting.id("request_painting"));
    public static final StreamCodec<FriendlyByteBuf, RequestPaintingC2S> CODEC = StreamCodec.ofMember(RequestPaintingC2S::encode, RequestPaintingC2S::decode);

    private void encode(FriendlyByteBuf buf) {
        buf.writeInt(id);
    }

    private static RequestPaintingC2S decode(FriendlyByteBuf buf) {
        return new RequestPaintingC2S(buf.readInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
