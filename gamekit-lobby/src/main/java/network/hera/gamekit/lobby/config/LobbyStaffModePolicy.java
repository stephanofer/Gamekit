package network.hera.gamekit.lobby.config;

import org.jetbrains.annotations.NotNull;

public record LobbyStaffModePolicy(
        boolean enabled,
        @NotNull String permission,
        @NotNull String gameMode,
        boolean clearInventoryOnEnable,
        boolean restoreLobbyOnDisable
) {

    public LobbyStaffModePolicy {
        gameMode = LobbyRuntimeConfig.requireKey("staff-mode.gamemode", gameMode);
        if (enabled && (permission == null || permission.isBlank())) {
            throw new IllegalArgumentException("staff-mode.permission cannot be blank when staff mode is enabled.");
        }
        permission = permission == null ? "" : permission.trim();
    }
}
