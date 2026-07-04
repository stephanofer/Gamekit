package network.hera.gamekit.lobby.config;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public record LobbyRuntimeConfig(
        boolean enabled,
        @NotNull String defaultLanguage,
        @NotNull LobbySpawn spawn,
        @NotNull LobbyJoinPolicy joinPolicy,
        @NotNull LobbyProtectionPolicy protectionPolicy,
        @NotNull LobbyWorldPolicy worldPolicy,
        @NotNull LobbyStaffModePolicy staffModePolicy,
        @NotNull LobbyCommandPolicy commandPolicy
) {

    public LobbyRuntimeConfig {
        defaultLanguage = requireKey("defaultLanguage", defaultLanguage);
        Objects.requireNonNull(spawn, "spawn");
        Objects.requireNonNull(joinPolicy, "joinPolicy");
        Objects.requireNonNull(protectionPolicy, "protectionPolicy");
        Objects.requireNonNull(worldPolicy, "worldPolicy");
        Objects.requireNonNull(staffModePolicy, "staffModePolicy");
        Objects.requireNonNull(commandPolicy, "commandPolicy");
    }

    static @NotNull String requireKey(@NotNull String field, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank.");
        }
        return value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
