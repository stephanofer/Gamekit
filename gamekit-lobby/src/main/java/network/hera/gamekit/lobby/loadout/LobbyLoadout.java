package network.hera.gamekit.lobby.loadout;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public final class LobbyLoadout {

    private final Map<String, LobbyItemDefinition> itemsById;
    private final Map<Integer, LobbyItemDefinition> itemsBySlot;

    public LobbyLoadout(@NotNull Collection<LobbyItemDefinition> items) {
        Objects.requireNonNull(items, "items");
        Map<String, LobbyItemDefinition> byId = new LinkedHashMap<>();
        Map<Integer, LobbyItemDefinition> bySlot = new LinkedHashMap<>();
        for (LobbyItemDefinition item : items) {
            if (byId.putIfAbsent(item.id(), item) != null) {
                throw new IllegalArgumentException("Duplicate lobby item id: " + item.id());
            }
            LobbyItemDefinition previous = bySlot.putIfAbsent(item.slot(), item);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate lobby item slot " + item.slot() + " for " + previous.id() + " and " + item.id());
            }
        }
        this.itemsById = Map.copyOf(byId);
        this.itemsBySlot = Map.copyOf(bySlot);
    }

    public @NotNull Collection<LobbyItemDefinition> items() {
        return this.itemsById.values();
    }

    public @NotNull Optional<LobbyItemDefinition> findById(@NotNull String id) {
        return Optional.ofNullable(this.itemsById.get(id));
    }

    public @NotNull Optional<LobbyItemDefinition> findBySlot(int slot) {
        return Optional.ofNullable(this.itemsBySlot.get(slot));
    }
}
