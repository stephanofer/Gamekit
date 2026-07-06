package network.hera.gamekit.infra.craftkit.redis;

import com.hera.craftkit.redis.RedisClient;
import java.util.Objects;
import network.hera.gamekit.network.transfer.TransferRequest;
import org.jetbrains.annotations.NotNull;

public final class CraftKitRedisTransferSignals {

    private CraftKitRedisTransferSignals() {
    }

    public static @NotNull String channel(@NotNull RedisClient redis) {
        return Objects.requireNonNull(redis, "redis").channel("gamekit", "transfer-request");
    }

    public static @NotNull String encode(@NotNull TransferRequest request) {
        return NetworkRedisPayloads.transferRequest(Objects.requireNonNull(request, "request"));
    }

    public static @NotNull TransferRequest decode(@NotNull String payload) {
        return NetworkRedisPayloads.transferRequest(Objects.requireNonNull(payload, "payload"));
    }
}
