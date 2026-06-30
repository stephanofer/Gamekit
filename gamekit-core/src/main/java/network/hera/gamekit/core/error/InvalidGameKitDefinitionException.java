package network.hera.gamekit.core.error;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class InvalidGameKitDefinitionException extends GameKitException {

    public InvalidGameKitDefinitionException(@NotNull String definition, @NotNull String reason) {
        super(CoreErrorCode.INVALID_DEFINITION, definition + " is invalid: " + reason, attributes(definition, reason));
    }

    private static @NotNull Map<String, @Nullable Object> attributes(@NotNull String definition, @NotNull String reason) {
        Map<String, @Nullable Object> attributes = new LinkedHashMap<>();
        attributes.put("definition", definition);
        attributes.put("reason", reason);
        return attributes;
    }
}
