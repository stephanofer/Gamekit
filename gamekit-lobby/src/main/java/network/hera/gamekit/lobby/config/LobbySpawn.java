package network.hera.gamekit.lobby.config;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public record LobbySpawn(
        @NotNull String world,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) {

    public LobbySpawn {
        Objects.requireNonNull(world, "world");
        if (world.isBlank()) {
            throw new IllegalArgumentException("Lobby spawn world cannot be blank.");
        }
    }
}
