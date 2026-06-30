package network.hera.gamekit.core.error;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class InvalidGameKitStateException extends GameKitException {

    public InvalidGameKitStateException(@NotNull String state, @NotNull String reason) {
        super(CoreErrorCode.INVALID_STATE, "Invalid GameKit state " + state + ": " + reason, attributes(state, reason));
    }

    private static @NotNull Map<String, @Nullable Object> attributes(@NotNull String state, @NotNull String reason) {
        Map<String, @Nullable Object> attributes = new LinkedHashMap<>();
        attributes.put("state", state);
        attributes.put("reason", reason);
        return attributes;
    }
}
