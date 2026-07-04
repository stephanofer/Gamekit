package network.hera.gamekit.lobbypaper.runtime;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import network.hera.gamekit.paper.player.PaperPlayerOperations;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

final class LobbyStaffModeService {

    private final PaperPlayerOperations players;
    private final Map<UUID, GameMode> previousModes = new ConcurrentHashMap<>();

    LobbyStaffModeService(@NotNull PaperPlayerOperations players) {
        this.players = players;
    }

    void enable(@NotNull Player player, @NotNull GameMode staffMode, boolean clearInventory) {
        this.previousModes.putIfAbsent(player.getUniqueId(), player.getGameMode());
        if (clearInventory) {
            this.players.clearInventory(player);
        }
        this.players.setGameMode(player, staffMode);
    }

    boolean disable(@NotNull Player player) {
        GameMode previous = this.previousModes.remove(player.getUniqueId());
        if (previous == null) {
            return false;
        }
        this.players.clearInventory(player);
        this.players.setGameMode(player, previous);
        return true;
    }

    boolean isEnabled(@NotNull Player player) {
        return this.previousModes.containsKey(player.getUniqueId());
    }

    void clear(@NotNull Player player) {
        this.previousModes.remove(player.getUniqueId());
    }
}
