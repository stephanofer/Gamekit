package network.hera.gamekit.lobbypaper.item;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public record PaperLobbyItemKeys(@NotNull NamespacedKey itemId) {

    public static @NotNull PaperLobbyItemKeys create(@NotNull Plugin plugin) {
        return new PaperLobbyItemKeys(new NamespacedKey(plugin, "gamekit_lobby_item"));
    }
}
