package network.hera.gamekit.lobbypaper.language;

import com.stephanofer.networkplayersettings.settings.api.PlayerSettingsService;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class NetworkPlayerSettingsLanguageResolver implements LobbyLanguageResolver {

    private final PlayerSettingsService settings;
    private final String defaultLanguage;

    public NetworkPlayerSettingsLanguageResolver(@NotNull PlayerSettingsService settings, @NotNull String defaultLanguage) {
        this.settings = Objects.requireNonNull(settings, "settings");
        this.defaultLanguage = normalize(defaultLanguage);
    }

    @Override
    public @NotNull String resolveLanguage(@NotNull Player player) {
        Objects.requireNonNull(player, "player");
        if (!this.settings.isReady(player.getUniqueId())) {
            return this.defaultLanguage;
        }
        return normalize(this.settings.resolvedLanguage(player).code());
    }

    private static @NotNull String normalize(@NotNull String language) {
        if (language.isBlank()) {
            throw new IllegalArgumentException("language cannot be blank.");
        }
        return language.trim().toLowerCase(Locale.ROOT);
    }
}
