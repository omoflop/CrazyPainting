package com.github.omoflop.crazypainting.network.types;

import com.github.omoflop.crazypainting.content.CrazyNetworking;
import net.minecraft.network.PacketByteBuf;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

public record ChangeKey(byte[] key) {
    public void writeTo(PacketByteBuf buf) {
        CrazyNetworking.writeByteArray(buf, key);
    }

    public static ChangeKey readFrom(PacketByteBuf buf) {
        return new ChangeKey(CrazyNetworking.readByteArray(buf));
    }

    public static ChangeKey create() {
        byte[] key = new byte[16];
        SecureRandom rng = new SecureRandom();
        rng.nextBytes(key);
        return new ChangeKey(key);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChangeKey key1 = (ChangeKey) o;
        return Objects.deepEquals(key, key1.key);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(key);
    }
}
