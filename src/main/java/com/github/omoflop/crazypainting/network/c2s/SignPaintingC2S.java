package com.github.omoflop.crazypainting.network.c2s;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.network.types.ChangeKey;
import com.github.omoflop.crazypainting.network.types.PaintingId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SignPaintingC2S (ChangeKey changeKey, PaintingId id, int easelEntityId, String title) implements CustomPacketPayload {
    public static final Type<SignPaintingC2S> ID = new Type<>(CrazyPainting.id("painting_sign"));
    public static final StreamCodec<FriendlyByteBuf, SignPaintingC2S> CODEC = StreamCodec.ofMember(SignPaintingC2S::encode, SignPaintingC2S::decode);

    private void encode(FriendlyByteBuf buf) {
        changeKey.writeTo(buf);
        id.writeTo(buf);
        buf.writeInt(easelEntityId);
        buf.writeUtf(title, 32);
    }

    private static SignPaintingC2S decode(FriendlyByteBuf buf) {
        return new SignPaintingC2S(
                ChangeKey.readFrom(buf),
                PaintingId.readFrom(buf),
                buf.readInt(),
                buf.readUtf(32)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
