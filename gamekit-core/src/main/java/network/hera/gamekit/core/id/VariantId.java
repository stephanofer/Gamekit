package network.hera.gamekit.core.id;

import org.jetbrains.annotations.NotNull;

public record VariantId(@NotNull String value) {

    public VariantId {
        value = IdFormats.requireDomainId("VariantId", value);
    }

    public static @NotNull VariantId of(@NotNull String value) {
        return new VariantId(value);
    }

    @Override
    public @NotNull String toString() {
        return this.value;
    }
}
