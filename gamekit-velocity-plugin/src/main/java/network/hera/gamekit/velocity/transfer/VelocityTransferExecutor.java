package network.hera.gamekit.velocity.transfer;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import network.hera.gamekit.core.result.Decision;
import network.hera.gamekit.network.transfer.TransferRejectReason;
import network.hera.gamekit.network.transfer.TransferRequest;
import org.jetbrains.annotations.NotNull;

public final class VelocityTransferExecutor {

    private final ProxyServer proxy;
    private final VelocityServerResolver serverResolver;
    private final AdmissionTransferValidator validator;
    private final InFlightTransfers inFlightTransfers;
    private final Clock clock;
    private final long timeoutMillis;

    public VelocityTransferExecutor(
            @NotNull ProxyServer proxy,
            @NotNull VelocityServerResolver serverResolver,
            @NotNull AdmissionTransferValidator validator,
            @NotNull InFlightTransfers inFlightTransfers,
            @NotNull Clock clock,
            long timeoutMillis
    ) {
        this.proxy = Objects.requireNonNull(proxy, "proxy");
        this.serverResolver = Objects.requireNonNull(serverResolver, "serverResolver");
        this.validator = Objects.requireNonNull(validator, "validator");
        this.inFlightTransfers = Objects.requireNonNull(inFlightTransfers, "inFlightTransfers");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.timeoutMillis = timeoutMillis;
    }

    public @NotNull CompletableFuture<Decision<TransferRejectReason>> transfer(@NotNull TransferRequest request) {
        Objects.requireNonNull(request, "request");
        if (this.proxy.isShuttingDown()) {
            return CompletableFuture.completedFuture(Decision.reject(TransferRejectReason.SHUTTING_DOWN));
        }
        return this.validator.validate(request, Instant.now(this.clock)).thenCompose(validation -> {
            if (validation.rejected()) {
                return CompletableFuture.completedFuture(validation);
            }
            final Player player = this.proxy.getPlayer(request.playerId().value()).orElse(null);
            if (player == null) {
                return CompletableFuture.completedFuture(Decision.reject(TransferRejectReason.PLAYER_NOT_ONLINE));
            }
            final RegisteredServer target = this.serverResolver.find(request.targetServerId()).orElse(null);
            if (target == null) {
                return CompletableFuture.completedFuture(Decision.reject(TransferRejectReason.TARGET_SERVER_UNAVAILABLE));
            }
            this.inFlightTransfers.remember(request, Instant.now(this.clock));
            return player.createConnectionRequest(target)
                .connect()
                .orTimeout(this.timeoutMillis, TimeUnit.MILLISECONDS)
                .thenApply(this::decisionFromResult)
                .exceptionally(ignored -> Decision.reject(TransferRejectReason.TRANSFER_FAILED))
                .whenComplete((decision, ignored) -> this.inFlightTransfers.forget(request.admissionRequestId()));
        });
    }

    private @NotNull Decision<TransferRejectReason> decisionFromResult(@NotNull ConnectionRequestBuilder.Result result) {
        final ConnectionRequestBuilder.Status status = result.getStatus();
        if (ConnectionStatusMapper.accepted(status)) {
            return Decision.accept();
        }
        return Decision.reject(ConnectionStatusMapper.rejectReason(status));
    }
}
