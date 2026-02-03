package com.github.omoflop.crazypainting.network.event;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.network.ChangeRecord;
import com.github.omoflop.crazypainting.network.types.ChangeKey;
import com.github.omoflop.crazypainting.network.types.PaintingData;
import com.github.omoflop.crazypainting.network.types.PaintingId;
import com.github.omoflop.crazypainting.state.CanvasManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class ServerPaintingChangeEventHandler implements ServerPlayNetworking.PlayPayloadHandler<PaintingChangeEvent> {
    @Override
    public void receive(PaintingChangeEvent packet, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        MinecraftServer server = Objects.requireNonNull(player.level()).getServer();

        // Ensure the player may be sending this data
        UUID uuid = player.getUUID();
        if (!CanvasManager.CHANGE_IDS.containsKey(uuid)) {
            printFail(player, "Server does not have a change key initialized for this player.");
            return;
        }

        // Does the data come with a change key?
        if (packet.change().isEmpty()) {
            printFail(player, "Player sent no change key.");
            return;
        }

        // Get the change key the player sent
        ChangeKey key = packet.change().get();

        // Look up in the change ids map to see if it matches the ID sent to the player initially
        // If not equal, return because the player sent the wrong ID
        ChangeRecord change = CanvasManager.CHANGE_IDS.get(uuid);
        if (!change.key().equals(key)) {
            printFail(player, "Change key does not match server key. Client sent: %s, Server has: %s", Arrays.toString(key.key()), Arrays.toString(change.key().key()));
            return;
        }

        // This is the id of the painting the player is updating
        PaintingId id = change.id();

        // Data sent by player for the painting update
        PaintingData data = packet.data();

        // If the id of the painting the client wants to edit does not match the one stored on the server,
        // return.
        if (!id.equals(data.id())) {
            printFail(player, "Received two different ids. Client sent: %s, Server has: %s", data.id(), id);
            return;
        }

        try {
            CanvasManager.receive(data, server, player);
        } catch (IOException e) {
            printFail(player, key, id, "Encountered exception: %s", e);
        }
    }

    private static void printFail(ServerPlayer player, String extras, Object... params) {
        CrazyPainting.LOGGER.log(Level.ERROR, "Failed to receive painting data from %s. Reason: %s".formatted(player.getName().getString(), String.format(extras, params)));
    }

    private static void printFail(ServerPlayer player, ChangeKey key, PaintingId id, String extras, Object... params) {
        if (!CrazyPainting.SHOW_DEBUG_LOGS) return;;
        CrazyPainting.LOGGER.log(Level.ERROR, "Failed to receive painting data from %s. Key: '%s', Id: '%s', Reason: %s".formatted(
                player.getName().getString(),
                Arrays.toString(key.key()),
                id.value(),
                String.format(extras, params)));
    }
}