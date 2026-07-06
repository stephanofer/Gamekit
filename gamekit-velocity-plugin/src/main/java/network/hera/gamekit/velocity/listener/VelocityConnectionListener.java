package network.hera.gamekit.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.velocity.transfer.InFlightTransfers;
import org.jetbrains.annotations.NotNull;

public final class VelocityConnectionListener {

    private final InFlightTransfers inFlightTransfers;
    private final Clock clock;
    private final boolean strictValidation;
    private final Set<ServerId> protectedServerIds;

    public VelocityConnectionListener(
            @NotNull InFlightTransfers inFlightTransfers,
            @NotNull Clock clock,
            boolean strictValidation,
            @NotNull Set<ServerId> protectedServerIds
    ) {
        this.inFlightTransfers = Objects.requireNonNull(inFlightTransfers, "inFlightTransfers");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.strictValidation = strictValidation;
        this.protectedServerIds = Set.copyOf(Objects.requireNonNull(protectedServerIds, "protectedServerIds"));
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        if (!this.strictValidation) {
            return;
        }
        final ServerId targetServerId = serverId(event.getOriginalServer());
        if (targetServerId == null || !this.protectedServerIds.contains(targetServerId)) {
            return;
        }
        final PlayerId playerId = PlayerId.of(event.getPlayer().getUniqueId());
        if (!this.inFlightTransfers.matches(playerId, targetServerId, Instant.now(this.clock))) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
    }

    private ServerId serverId(RegisteredServer server) {
        try {
            return ServerId.of(server.getServerInfo().getName());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
