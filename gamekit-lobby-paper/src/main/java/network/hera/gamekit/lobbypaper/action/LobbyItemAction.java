package network.hera.gamekit.lobbypaper.action;

import java.util.concurrent.CompletableFuture;
import network.hera.gamekit.lobby.loadout.LobbyActionDefinition;
import network.hera.gamekit.lobby.loadout.LobbyClickType;
import network.hera.gamekit.lobby.loadout.LobbyItemDefinition;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface LobbyItemAction {

    @NotNull CompletableFuture<Void> execute(@NotNull Context context);

    record Context(
            @NotNull Player player,
            @NotNull LobbyItemDefinition item,
            @NotNull LobbyClickType clickType,
            @NotNull LobbyActionDefinition definition
    ) {
    }
}
