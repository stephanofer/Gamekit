package network.hera.gamekit.lobby.loadout;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public record LobbyItemDefinition(
        @NotNull String id,
        int slot,
        @NotNull String material,
        @NotNull Map<String, LocalizedLobbyItemText> textByLanguage,
        @NotNull Map<LobbyClickType, LobbyActionDefinition> actions
) {

    public LobbyItemDefinition {
        id = requireId(id);
        if (slot < 0 || slot > 35) {
            throw new IllegalArgumentException("Lobby item slot must be between 0 and 35: " + id);
        }
        material = requireId(material).toUpperCase(Locale.ROOT);
        textByLanguage = normalizeText(textByLanguage);
        EnumMap<LobbyClickType, LobbyActionDefinition> normalizedActions = new EnumMap<>(LobbyClickType.class);
        normalizedActions.putAll(Objects.requireNonNull(actions, "actions"));
        actions = Map.copyOf(normalizedActions);
    }

    public @NotNull LocalizedLobbyItemText textFor(@NotNull String language, @NotNull String defaultLanguage) {
        LocalizedLobbyItemText text = this.textByLanguage.get(normalizeLanguage(language));
        if (text != null) {
            return text;
        }
        text = this.textByLanguage.get(normalizeLanguage(defaultLanguage));
        if (text != null) {
            return text;
        }
        return this.textByLanguage.values().iterator().next();
    }

    static @NotNull String normalizeLanguage(@NotNull String language) {
        return language.trim().toLowerCase(Locale.ROOT);
    }

    private static @NotNull Map<String, LocalizedLobbyItemText> normalizeText(@NotNull Map<String, LocalizedLobbyItemText> input) {
        Objects.requireNonNull(input, "textByLanguage");
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Lobby item textByLanguage cannot be empty.");
        }
        Map<String, LocalizedLobbyItemText> normalized = new LinkedHashMap<>();
        input.forEach((language, text) -> normalized.put(normalizeLanguage(language), Objects.requireNonNull(text, "text")));
        return Map.copyOf(normalized);
    }

    private static @NotNull String requireId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Lobby item id/material cannot be blank.");
        }
        return value.trim();
    }
}
