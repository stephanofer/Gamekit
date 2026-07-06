package network.hera.gamekit.velocity.transfer;

import com.hera.craftkit.redis.RedisClient;
import com.hera.craftkit.redis.RedisSubscription;
import java.util.Objects;
import network.hera.gamekit.core.result.Decision;
import network.hera.gamekit.infra.craftkit.redis.CraftKitRedisTransferSignals;
import network.hera.gamekit.network.transfer.TransferRejectReason;
import network.hera.gamekit.network.transfer.TransferRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public final class VelocityTransferSubscriber implements AutoCloseable {

    private final RedisSubscription subscription;

    public VelocityTransferSubscriber(
            @NotNull RedisClient redis,
            @NotNull VelocityTransferExecutor executor,
            @NotNull Logger logger
    ) {
        Objects.requireNonNull(redis, "redis");
        Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(logger, "logger");
        this.subscription = redis.subscriber().subscribe(CraftKitRedisTransferSignals.channel(redis), message -> {
            final TransferRequest request;
            try {
                request = CraftKitRedisTransferSignals.decode(message.payload());
            } catch (RuntimeException exception) {
                logger.warn("Ignored malformed GameKit transfer request payload.", exception);
                return;
            }
            executor.transfer(request).whenComplete((decision, throwable) -> {
                if (throwable != null) {
                    logger.warn("GameKit transfer request failed unexpectedly for player {} to {}.", request.playerId(), request.targetServerId(), throwable);
                    return;
                }
                logDecision(logger, request, decision);
            });
        });
    }

    private static void logDecision(Logger logger, TransferRequest request, Decision<TransferRejectReason> decision) {
        if (decision.accepted()) {
            logger.info("GameKit transfer accepted: player={} target={} admission={}", request.playerId(), request.targetServerId(), request.admissionRequestId());
            return;
        }
        logger.warn(
            "GameKit transfer rejected: player={} target={} admission={} reason={}",
            request.playerId(),
            request.targetServerId(),
            request.admissionRequestId(),
            decision.requireRejectReason()
        );
    }

    @Override
    public void close() {
        this.subscription.close();
    }
}
