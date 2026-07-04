package network.hera.gamekit.lobby.config;

import org.jetbrains.annotations.NotNull;

public record LobbyJoinPolicy(
        boolean clearInventory,
        @NotNull ResetPlayer resetPlayer,
        boolean teleportSpawn,
        @NotNull SetGameMode setGameMode,
        boolean applyLoadout,
        @NotNull HeldSlot heldSlot
) {

    public LobbyJoinPolicy {
        java.util.Objects.requireNonNull(resetPlayer, "resetPlayer");
        java.util.Objects.requireNonNull(setGameMode, "setGameMode");
        java.util.Objects.requireNonNull(heldSlot, "heldSlot");
    }

    public record ResetPlayer(
            boolean enabled,
            boolean health,
            boolean food,
            boolean saturation,
            boolean fire,
            boolean freeze,
            boolean fallDistance,
            boolean remainingAir,
            boolean potionEffects,
            boolean exp
    ) {
    }

    public record SetGameMode(boolean enabled, @NotNull String value) {

        public SetGameMode {
            value = LobbyRuntimeConfig.requireKey("join.set-gamemode.value", value);
        }
    }

    public record HeldSlot(boolean enabled, int slot) {

        public HeldSlot {
            if (slot < 0 || slot > 8) {
                throw new IllegalArgumentException("join.held-slot.slot must be between 0 and 8.");
            }
        }
    }
}
