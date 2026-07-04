package network.hera.gamekit.lobby.config;

import org.jetbrains.annotations.NotNull;

public record LobbyCommandPolicy(
        boolean enabled,
        @NotNull String root,
        @NotNull String permissionPrefix
) {

    public LobbyCommandPolicy {
        root = LobbyRuntimeConfig.requireKey("commands.root", root);
        permissionPrefix = LobbyRuntimeConfig.requireKey("commands.permission-prefix", permissionPrefix);
    }
}
