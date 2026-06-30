package network.hera.gamekit.core.error;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameKitException extends RuntimeException {

    private final GameKitErrorCode errorCode;
    private final Map<String, @Nullable Object> attributes;

    public GameKitException(
            @NotNull GameKitErrorCode errorCode,
            @NotNull String message,
            @NotNull Map<String, @Nullable Object> attributes
    ) {
        this(errorCode, message, attributes, null);
    }

    public GameKitException(
            @NotNull GameKitErrorCode errorCode,
            @NotNull String message,
            @NotNull Map<String, @Nullable Object> attributes,
            @Nullable Throwable cause
    ) {
        super(message, cause);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode");
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(attributes, "attributes")));
    }

    public @NotNull GameKitErrorCode errorCode() {
        return this.errorCode;
    }

    public @NotNull Map<String, @Nullable Object> attributes() {
        return this.attributes;
    }
}
