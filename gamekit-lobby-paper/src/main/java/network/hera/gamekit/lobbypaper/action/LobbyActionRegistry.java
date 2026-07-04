package network.hera.gamekit.lobbypaper.action;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public final class LobbyActionRegistry {

    private final Map<String, LobbyItemAction> actions;

    private LobbyActionRegistry(@NotNull Map<String, LobbyItemAction> actions) {
        this.actions = Map.copyOf(actions);
    }

    public static @NotNull Builder create() {
        return new Builder();
    }

    public @NotNull Optional<LobbyItemAction> find(@NotNull String type) {
        return Optional.ofNullable(this.actions.get(type));
    }

    public static final class Builder {

        private final Map<String, LobbyItemAction> actions = new LinkedHashMap<>();

        public @NotNull Builder register(@NotNull String type, @NotNull LobbyItemAction action) {
            Objects.requireNonNull(action, "action");
            String normalized = normalize(type);
            if (this.actions.putIfAbsent(normalized, action) != null) {
                throw new IllegalArgumentException("Duplicate lobby action type: " + normalized);
            }
            return this;
        }

        public @NotNull LobbyActionRegistry build() {
            return new LobbyActionRegistry(this.actions);
        }

        private static @NotNull String normalize(@NotNull String type) {
            if (type.isBlank()) {
                throw new IllegalArgumentException("Lobby action type cannot be blank.");
            }
            return type.trim().toLowerCase(java.util.Locale.ROOT);
        }
    }
}
