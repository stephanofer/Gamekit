package network.hera.gamekit.lobby.loadout;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record LobbyActionDefinition(
        @NotNull String type,
        @NotNull Map<String, String> arguments
) {

    public LobbyActionDefinition {
        type = requireActionKey(type);
        Objects.requireNonNull(arguments, "arguments");
        arguments = Collections.unmodifiableMap(new LinkedHashMap<>(arguments));
    }

    public @Nullable String argument(@NotNull String key) {
        return this.arguments.get(key);
    }

    private static @NotNull String requireActionKey(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Lobby action type cannot be blank.");
        }
        return value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
