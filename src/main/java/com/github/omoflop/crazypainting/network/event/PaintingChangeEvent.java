package com.github.omoflop.crazypainting.network.event;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.network.types.ChangeKey;
import com.github.omoflop.crazypainting.network.types.PaintingData;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PaintingChangeEvent(Optional<ChangeKey> change, PaintingData data, String title, int easelEntityId) implements CustomPacketPayload {
    public static final Type<PaintingChangeEvent> ID = new Type<>(CrazyPainting.id("painting_change"));
    public static final StreamCodec<FriendlyByteBuf, PaintingChangeEvent> CODEC = StreamCodec.ofMember(PaintingChangeEvent::encode, PaintingChangeEvent::decode);

    private void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(change.isPresent());
        change.ifPresent(changeId -> changeId.writeTo(buf));
        data.writeTo(buf);

        buf.writeUtf(title);
        buf.writeInt(easelEntityId);
    }

    private static PaintingChangeEvent decode(FriendlyByteBuf buf) {
        boolean changeIsPresent = buf.readBoolean();

        return new PaintingChangeEvent(
                changeIsPresent ? Optional.of(ChangeKey.readFrom(buf)) : Optional.empty(),
                PaintingData.readFrom(buf),
                buf.readUtf(32),
                buf.readInt()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
