package network.hera.gamekit.testkit.network;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import network.hera.gamekit.core.time.GameKitClock;
import network.hera.gamekit.network.admission.AdmissionRequest;
import network.hera.gamekit.network.admission.AdmissionRequestId;
import network.hera.gamekit.network.admission.AdmissionStore;
import org.jetbrains.annotations.NotNull;

public final class FakeAdmissionStore implements AdmissionStore {

    private final GameKitClock clock;
    private final Map<AdmissionRequestId, AdmissionRequest> admissions = new ConcurrentHashMap<>();

    public FakeAdmissionStore(@NotNull GameKitClock clock) {
        this.clock = clock;
    }

    @Override
    public @NotNull CompletableFuture<Void> save(@NotNull AdmissionRequest request) {
        this.admissions.put(request.id(), request);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull CompletableFuture<Optional<AdmissionRequest>> find(@NotNull AdmissionRequestId requestId) {
        final AdmissionRequest request = this.admissions.get(requestId);
        if (request == null || request.expiredAt(this.clock.now())) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return CompletableFuture.completedFuture(Optional.of(request));
    }

    @Override
    public @NotNull CompletableFuture<Optional<AdmissionRequest>> consume(@NotNull AdmissionRequestId requestId) {
        final AdmissionRequest request = this.admissions.remove(requestId);
        if (request == null || request.expiredAt(this.clock.now())) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return CompletableFuture.completedFuture(Optional.of(request));
    }

    public boolean contains(@NotNull AdmissionRequestId requestId) {
        final AdmissionRequest request = this.admissions.get(requestId);
        final Instant now = this.clock.now();
        return request != null && !request.expiredAt(now);
    }
}
