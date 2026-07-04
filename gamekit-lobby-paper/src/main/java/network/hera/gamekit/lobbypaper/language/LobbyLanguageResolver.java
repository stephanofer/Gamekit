package network.hera.gamekit.lobbypaper.language;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface LobbyLanguageResolver {

    @NotNull String resolveLanguage(@NotNull Player player);
}
