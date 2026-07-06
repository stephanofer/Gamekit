package network.hera.gamekit.velocity.fallback;

import java.util.Optional;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.network.fallback.FallbackPolicy;
import network.hera.gamekit.network.transfer.TransferRejectReason;
import org.jetbrains.annotations.NotNull;

final class ConfiguredVelocityFallbackPolicy implements FallbackPolicy {

    private final ServerId defaultLobbyServerId;

    ConfiguredVelocityFallbackPolicy(ServerId defaultLobbyServerId) {
        this.defaultLobbyServerId = defaultLobbyServerId;
    }

    @Override
    public @NotNull Optional<ServerId> fallbackFor(@NotNull PlayerId playerId, @NotNull TransferRejectReason reason) {
        return Optional.ofNullable(this.defaultLobbyServerId);
    }
}
