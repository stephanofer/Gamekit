package network.hera.gamekit.velocity.transfer;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.network.admission.AdmissionRequestId;
import network.hera.gamekit.network.transfer.TransferRequest;
import org.jetbrains.annotations.NotNull;

public final class InFlightTransfers {

    private final Duration ttl;
    private final Map<AdmissionRequestId, Entry> entries = new ConcurrentHashMap<>();

    public InFlightTransfers(@NotNull Duration ttl) {
        this.ttl = Objects.requireNonNull(ttl, "ttl");
    }

    public void remember(@NotNull TransferRequest request, @NotNull Instant now) {
        cleanup(now);
        this.entries.put(request.admissionRequestId(), new Entry(request.playerId(), request.targetServerId(), now.plus(this.ttl)));
    }

    public boolean matches(@NotNull PlayerId playerId, @NotNull ServerId targetServerId, @NotNull Instant now) {
        cleanup(now);
        return this.entries.values().stream()
            .anyMatch(entry -> entry.playerId.equals(playerId) && entry.targetServerId.equals(targetServerId));
    }

    public void forget(@NotNull AdmissionRequestId admissionRequestId) {
        this.entries.remove(Objects.requireNonNull(admissionRequestId, "admissionRequestId"));
    }

    private void cleanup(@NotNull Instant now) {
        this.entries.entrySet().removeIf(entry -> !entry.getValue().expiresAt.isAfter(now));
    }

    private record Entry(@NotNull PlayerId playerId, @NotNull ServerId targetServerId, @NotNull Instant expiresAt) {
    }
}
