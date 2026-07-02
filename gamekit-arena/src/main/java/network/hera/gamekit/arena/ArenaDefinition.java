package network.hera.gamekit.arena;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import network.hera.gamekit.core.id.ArenaId;
import network.hera.gamekit.core.id.GameId;
import org.jetbrains.annotations.NotNull;

public record ArenaDefinition(
        @NotNull ArenaId arenaId,
        @NotNull String templateId,
        @NotNull GameId gameId,
        @NotNull Set<String> tags,
        int maxPlayers,
        boolean enabled
) {

    private static final Pattern TAG_FORMAT = Pattern.compile("[a-z0-9]+(?:_[a-z0-9]+)*");

    public ArenaDefinition {
        Objects.requireNonNull(arenaId, "arenaId");
        templateId = requireText("templateId", templateId);
        Objects.requireNonNull(gameId, "gameId");
        tags = copyTags(tags);
        requirePositiveMaxPlayers(maxPlayers);
    }

    private static void requirePositiveMaxPlayers(final int maxPlayers) {
        if (maxPlayers <= 0) {
            throw new IllegalArgumentException("Arena maxPlayers must be greater than zero.");
        }
    }

    static @NotNull String requireText(final String field, final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank.");
        }
        return value;
    }

    static @NotNull Set<String> copyTags(final Set<String> tags) {
        Objects.requireNonNull(tags, "tags");
        tags.forEach(ArenaDefinition::requireTag);
        return Set.copyOf(tags);
    }

    private static void requireTag(final String tag) {
        requireText("tag", tag);
        if (!TAG_FORMAT.matcher(tag).matches()) {
            throw new IllegalArgumentException("Arena tag must use lowercase_snake_case: " + tag);
        }
    }
}
