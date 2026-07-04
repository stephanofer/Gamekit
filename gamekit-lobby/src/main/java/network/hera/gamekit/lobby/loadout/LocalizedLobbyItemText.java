package network.hera.gamekit.lobby.loadout;

import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public record LocalizedLobbyItemText(@NotNull String name, @NotNull List<String> lore) {

    public LocalizedLobbyItemText {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Lobby item text name cannot be blank.");
        }
        lore = List.copyOf(Objects.requireNonNull(lore, "lore"));
    }
}
