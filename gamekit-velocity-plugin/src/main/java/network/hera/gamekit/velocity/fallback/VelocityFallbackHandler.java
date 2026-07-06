package network.hera.gamekit.velocity.fallback;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.network.fallback.FallbackPolicy;
import network.hera.gamekit.network.transfer.TransferRejectReason;
import network.hera.gamekit.velocity.config.VelocityRuntimeConfig;
import network.hera.gamekit.velocity.transfer.InFlightTransfers;
import network.hera.gamekit.velocity.transfer.VelocityServerResolver;
import org.jetbrains.annotations.NotNull;

public final class VelocityFallbackHandler {

    private static final Component FALLBACK_MESSAGE = Component.text("Connection failed. Sending you to a safe lobby.");
    private static final Component DISCONNECT_MESSAGE = Component.text("Connection failed and no fallback server is available.");

    private final VelocityServerResolver serverResolver;
    private final InFlightTransfers inFlightTransfers;
    private final Clock clock;
    private final FallbackPolicy fallbackPolicy;
    private final boolean disconnectIfNoFallback;

    public VelocityFallbackHandler(
            @NotNull VelocityServerResolver serverResolver,
            @NotNull InFlightTransfers inFlightTransfers,
            @NotNull Clock clock,
            @NotNull VelocityRuntimeConfig.FallbackSettings fallbackSettings
    ) {
        this.serverResolver = Objects.requireNonNull(serverResolver, "serverResolver");
        this.inFlightTransfers = Objects.requireNonNull(inFlightTransfers, "inFlightTransfers");
        this.clock = Objects.requireNonNull(clock, "clock");
        Objects.requireNonNull(fallbackSettings, "fallbackSettings");
        this.fallbackPolicy = new ConfiguredVelocityFallbackPolicy(fallbackSettings.defaultLobbyServerId());
        this.disconnectIfNoFallback = fallbackSettings.disconnectIfNoFallback();
    }

    @Subscribe
    public void onKickedFromServer(KickedFromServerEvent event) {
        if (!event.kickedDuringServerConnect()) {
            return;
        }
        final ServerId kickedServerId = serverId(event.getServer());
        final PlayerId playerId = PlayerId.of(event.getPlayer().getUniqueId());
        if (kickedServerId == null || !this.inFlightTransfers.matches(playerId, kickedServerId, Instant.now(this.clock))) {
            return;
        }
        final ServerId fallbackServerId = this.fallbackPolicy
            .fallbackFor(playerId, TransferRejectReason.TRANSFER_FAILED)
            .orElse(null);
        if (fallbackServerId != null) {
            final RegisteredServer fallback = this.serverResolver.find(fallbackServerId).orElse(null);
            if (fallback != null) {
                event.setResult(KickedFromServerEvent.RedirectPlayer.create(fallback, FALLBACK_MESSAGE));
                return;
            }
        }
        if (this.disconnectIfNoFallback) {
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(DISCONNECT_MESSAGE));
        } else {
            event.setResult(KickedFromServerEvent.Notify.create(DISCONNECT_MESSAGE));
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
