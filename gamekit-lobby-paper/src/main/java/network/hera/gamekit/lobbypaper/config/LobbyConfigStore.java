package network.hera.gamekit.lobbypaper.config;

import network.hera.gamekit.lobby.config.LobbyRuntimeConfig;
import network.hera.gamekit.lobby.loadout.LobbyLoadout;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface LobbyConfigStore {

    void setSpawn(@NotNull Location location);

    void save();

    @NotNull LobbyRuntimeConfig reloadConfig();

    @NotNull LobbyLoadout reloadLoadout();
}
