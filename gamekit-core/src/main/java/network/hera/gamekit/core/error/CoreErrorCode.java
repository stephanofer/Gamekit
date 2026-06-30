package network.hera.gamekit.core.error;

import org.jetbrains.annotations.NotNull;

public enum CoreErrorCode implements GameKitErrorCode {
    INVALID_ID("core.invalid_id"),
    INVALID_DEFINITION("core.invalid_definition"),
    INVALID_STATE("core.invalid_state");

    private final String code;

    CoreErrorCode(String code) {
        this.code = code;
    }

    @Override
    public @NotNull String code() {
        return this.code;
    }
}
