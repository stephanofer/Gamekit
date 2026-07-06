package network.hera.gamekit.infra.craftkit.redis;

import com.hera.craftkit.redis.RedisClient;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import network.hera.gamekit.core.result.Decision;
import network.hera.gamekit.network.transfer.TransferGateway;
import network.hera.gamekit.network.transfer.TransferRejectReason;
import network.hera.gamekit.network.transfer.TransferRequest;
import org.jetbrains.annotations.NotNull;

public final class CraftKitRedisTransferGateway implements TransferGateway {

    private final RedisClient redis;

    public CraftKitRedisTransferGateway(@NotNull RedisClient redis) {
        this.redis = Objects.requireNonNull(redis, "redis");
    }

    @Override
    public @NotNull CompletableFuture<Decision<TransferRejectReason>> transfer(@NotNull TransferRequest request) {
        Objects.requireNonNull(request, "request");
        return this.redis.publisher()
            .publish(CraftKitRedisTransferSignals.channel(this.redis), CraftKitRedisTransferSignals.encode(request))
            .thenApply(CraftKitRedisTransferGateway::decisionFromSubscribers)
            .exceptionally(ignored -> Decision.reject(TransferRejectReason.REDIS_UNAVAILABLE));
    }

    static @NotNull Decision<TransferRejectReason> decisionFromSubscribers(long subscribers) {
        return subscribers > 0
            ? Decision.accept()
            : Decision.reject(TransferRejectReason.TRANSFER_SIGNAL_NOT_DELIVERED);
    }
}
