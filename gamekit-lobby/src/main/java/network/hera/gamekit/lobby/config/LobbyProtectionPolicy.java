package network.hera.gamekit.lobby.config;

public record LobbyProtectionPolicy(
        boolean inventory,
        boolean drop,
        boolean pickup,
        boolean offhand,
        boolean craft,
        boolean itemDamage,
        boolean blockPlace,
        boolean blockBreak,
        boolean blockInteract,
        boolean pvp,
        boolean fallDamage,
        boolean fireDamage,
        boolean drowning,
        boolean hunger,
        boolean voidTeleport,
        boolean mobSpawn,
        boolean mobDrops,
        boolean deathMessages,
        boolean weatherChange,
        boolean fireSpread,
        boolean leafDecay,
        boolean itemFrames
) {
}
