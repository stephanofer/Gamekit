package network.hera.gamekit.core.error;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class InvalidGameKitIdException extends GameKitException {

    private InvalidGameKitIdException(@NotNull String message, @NotNull Map<String, @Nullable Object> attributes) {
        super(CoreErrorCode.INVALID_ID, message, attributes);
    }

    public static @NotNull InvalidGameKitIdException invalid(
            @NotNull String type,
            @Nullable String value,
            @NotNull String expectedFormat
    ) {
        Map<String, @Nullable Object> attributes = new LinkedHashMap<>();
        attributes.put("type", type);
        attributes.put("value", value);
        attributes.put("expectedFormat", expectedFormat);
        return new InvalidGameKitIdException(type + " must use " + expectedFormat + " format", attributes);
    }
}
