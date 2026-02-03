package com.github.omoflop.crazypainting.network.s2c;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.network.types.PaintingId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateEaselCanvasIdS2C(int entityId, PaintingId id) implements CustomPacketPayload {
    public static final Type<UpdateEaselCanvasIdS2C> ID = new Type<>(CrazyPainting.id("update_easel_canvas_id"));
    public static final StreamCodec<FriendlyByteBuf, UpdateEaselCanvasIdS2C> CODEC = StreamCodec.ofMember(UpdateEaselCanvasIdS2C::encode, UpdateEaselCanvasIdS2C::decode);

    private void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        id.writeTo(buf);
    }

    private static UpdateEaselCanvasIdS2C decode(FriendlyByteBuf buf) {
        return new UpdateEaselCanvasIdS2C(buf.readInt(), PaintingId.readFrom(buf));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
